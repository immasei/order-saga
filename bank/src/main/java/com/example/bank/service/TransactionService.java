package com.example.bank.service;

import com.example.bank.dto.transaction.*;
import com.example.bank.entity.Transaction;
import com.example.bank.enums.TransactionType;
import com.example.bank.exception.ConflictException;
import com.example.bank.exception.InsufficientBalanceException;
import com.example.bank.entity.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService implements DtoMapper<Transaction, TransactionDTO, TransactionResponseDTO> {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public TransactionResponseDTO process(TransactionDTO dto) {
        // Check idempotency key
        String key = dto.getIdempotencyKey();
        if (key != null) {
            transactionRepository.findByIdempotencyKey(key).ifPresent(existing -> {
                throw new ConflictException("Duplicate transaction for idempotency key: " + key);
            });
        }

        Transaction tx = switch (dto.getTransactionType()) {
            case TRANSFER -> transfer((TransferDTO) dto);
            case DEPOSIT -> deposit((DepositDTO) dto);
            case WITHDRAWAL -> withdraw((WithdrawDTO) dto);
            case REFUND -> refund((RefundDTO) dto);
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + dto.getTransactionType());
        };

        return toResponse(tx);
    }

    @Transactional
    public Transaction transfer(TransferDTO transferDto) {
        String fromRef = transferDto.getFromAccountRef();
        String toRef = transferDto.getToAccountRef();

        // Lock both accounts in deterministic order
        List<Account> locked = accountRepository
            .lockOrThrowByRefsInDeterministicOrder(List.of(fromRef, toRef));

        // Map back to caller intent
        Account first  = locked.get(0);
        Account second = locked.get(1);
        Account from   = first.getAccountRef().equals(fromRef) ? first : second;
        Account to     = from == first ? second : first;

        BigDecimal amount = transferDto.getAmount();

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException("Insufficient funds in " + fromRef);

        from.modifyBalance(amount.negate());
        to.modifyBalance(amount);

        return createAndSaveTx(from, to, transferDto);
    }

    @Transactional
    public Transaction deposit(DepositDTO depositDto) {
        String toRef = depositDto.getToAccountRef();
        BigDecimal amount = depositDto.getAmount();

        Account to = accountRepository.lockOrThrowByRef(toRef);
        to.modifyBalance(amount);

        return createAndSaveTx(null, to, depositDto);
    }

    @Transactional
    public Transaction withdraw(WithdrawDTO withdrawDto) {
        String fromRef = withdrawDto.getFromAccountRef();
        BigDecimal amount = withdrawDto.getAmount();

        Account from = accountRepository.lockOrThrowByRef(fromRef);

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException();

        from.modifyBalance(amount.negate());

        return createAndSaveTx(from, null, withdrawDto);
    }

    @Transactional
    public Transaction refund(RefundDTO refundDto) {
        String originalRef = refundDto.getOriginalTransactionRef();

        // 1. Lock original to prevent double-refund races
        Transaction originalTx = transactionRepository.lockOrThrowByRef(originalRef);

        if (originalTx.isReversed()) {
            throw new ConflictException("Transaction already refunded: " + originalRef);
        }

        BigDecimal amount = originalTx.getAmount();
        Account from = originalTx.getFromAccount(); // may be null for DEPOSIT
        Account to = originalTx.getToAccount();     // may be null for WITHDRAWAL

        // 2. Perform the reverse movement with and flipped direction
        Transaction reversalTx;

        switch (originalTx.getTransactionType()) {
            case DEPOSIT -> {
                var req = modelMapper.map(originalTx, WithdrawDTO.class);
                req.setAmount(amount);
                req.setFromAccountRef(to.getAccountRef());
                reversalTx = withdraw(req);
            }
            case WITHDRAWAL -> {
                var req = modelMapper.map(originalTx, DepositDTO.class);
                req.setAmount(amount);
                req.setToAccountRef(from.getAccountRef());
                reversalTx = deposit(req);
            }
            case TRANSFER -> {
                var req = modelMapper.map(originalTx, TransferDTO.class);
                req.setAmount(amount);
                req.setFromAccountRef(to.getAccountRef());
                req.setToAccountRef(from.getAccountRef());
                reversalTx = transfer(req);
            }
            default -> throw new IllegalStateException("Unsupported type: " + originalTx.getTransactionType());
        };

        // 3. Mark original reversed and link (while still holding the lock)
        originalTx.setReversed(true);
        reversalTx.setReversalOf(originalTx);
        reversalTx.setMemo(TransactionType.REFUND + " " + refundDto.getMemo());
        transactionRepository.save(originalTx);

        return transactionRepository.save(reversalTx);
    }

    private Transaction createAndSaveTx(Account from, Account to, TransactionDTO dto) {
        Transaction tx = toEntity(dto);
        tx.setFromAccount(from);
        tx.setToAccount(to);

        tx.setMemo(dto.getTransactionType().toString() + " " + dto.getMemo());
        if (from != null) from.addOutgoingTransaction(tx);
        if (to != null) to.addIncomingTransaction(tx);

        return transactionRepository.saveAndFlush(tx);
    }

    @Override
    public Transaction toEntity(TransactionDTO dto) {
        return modelMapper.map(dto, Transaction.class);
    }

    @Override
    public TransactionResponseDTO toResponse(Transaction tx) {
        TransactionResponseDTO dto = modelMapper.map(tx, TransactionResponseDTO.class);
        Account from = tx.getFromAccount();
        Account to = tx.getToAccount();
        if (from != null) dto.setFromAccountRef(from.getAccountRef());
        if (to != null) dto.setToAccountRef(to.getAccountRef());
        return dto;
    }
}

