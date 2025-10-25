package com.example.bank.service;

import com.example.bank.dto.transaction.*;
import com.example.bank.entity.Transaction;
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
    public TransactionResponseDTO transfer(TransferDTO transferDto) {
        List<Account> accounts = accountRepository.lockOrThrow(
            List.of(transferDto.getFromAccountId(), transferDto.getToAccountId())
        );
        Account from = accounts.get(0);
        Account to = accounts.get(1);
        BigDecimal amount = transferDto.getAmount();

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException();

        from.modifyBalance(amount.negate());
        to.modifyBalance(amount);

        Transaction tx = createAndSaveTx(from, to, transferDto);
        return toResponse(tx);
    }

    @Transactional
    public TransactionResponseDTO deposit(DepositDTO depositDto) {
        Account to = accountRepository.lockOrThrow(depositDto.getToAccountId());
        BigDecimal amount = depositDto.getAmount();
        to.modifyBalance(amount);

        Transaction tx = createAndSaveTx(null, to, depositDto);
        return toResponse(tx);
    }

    @Transactional
    public TransactionResponseDTO withdraw(WithdrawDTO withdrawDto) {
        Account from = accountRepository.lockOrThrow(withdrawDto.getFromAccountId());
        BigDecimal amount = withdrawDto.getAmount();

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException();

        from.modifyBalance(amount.negate());
        Transaction tx = createAndSaveTx(from, null, withdrawDto);
        return toResponse(tx);
    }

    @Transactional
    public TransactionResponseDTO refund(RefundDTO refundDto) {
        Transaction original = transactionRepository.getOrThrow(refundDto.getOriginalTransactionId());
        BigDecimal amount = original.getAmount();
        Account from = original.getFromAccount();
        Account to = original.getToAccount();

        TransactionResponseDTO reversal = switch (original.getTransactionType()) {
            case DEPOSIT -> {
                // Reverse deposit = take money back out of 'to'
                DepositDTO depositReq = modelMapper.map(original, DepositDTO.class);
                depositReq.setToAccountId(to.getId());
                depositReq.setAmount(amount.negate());

                TransactionResponseDTO tx = deposit(depositReq);
                yield tx;
            }
            case WITHDRAWAL -> {
                // Reverse withdrawal = put money back into 'from'
                WithdrawDTO withdrawReq = modelMapper.map(original, WithdrawDTO.class);
                withdrawReq.setFromAccountId(from.getId());
                withdrawReq.setAmount(amount.negate());

                TransactionResponseDTO tx = withdraw(withdrawReq);
                yield tx;
            }
            case TRANSFER -> {
                // Reverse transfer = transfer back (swap sides)
                TransferDTO transferReq = modelMapper.map(original, TransferDTO.class);
                transferReq.setFromAccountId(to.getId());
                transferReq.setToAccountId(from.getId());

                TransactionResponseDTO tx = transfer(transferReq);
                yield tx;
            }
            default -> throw new IllegalStateException("Unsupported transaction type: " + original.getTransactionType());

        };

        reversal.setAmount(amount);
        reversal.setMemo(refundDto.getMemo());
        original.setReversed(true);
        reversal.setReversalOf(toResponse(original));

        reversal.setReversed(null);
        reversal.setToAccount(null);
        reversal.setFromAccount(null);

        return reversal;
    }

    private Transaction createAndSaveTx(Account from, Account to, TransactionDTO transactionDto) {
        Transaction tx = toEntity(transactionDto);
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx = transactionRepository.save(tx);
        if (from != null) from.addOutgoingTransaction(tx);
        if (to != null) to.addIncomingTransaction(tx);
        return tx;
    }

    @Override
    public Transaction toEntity(TransactionDTO dto) {
        return modelMapper.map(dto, Transaction.class);
    }

    @Override
    public TransactionResponseDTO toResponse(Transaction tx) {
        TransactionResponseDTO dto = modelMapper.map(tx, TransactionResponseDTO.class);
        return dto;
    }
}

