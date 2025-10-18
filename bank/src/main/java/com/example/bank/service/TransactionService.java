package com.example.bank.service;

import com.example.bank.dto.transaction.*;
import com.example.bank.entity.Transaction;
import com.example.bank.exception.InsufficientBalanceException;
import com.example.bank.entity.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TransactionService implements DtoMapper<Transaction, TransactionRequest, TransactionResponse> {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              ModelMapper modelMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.modelMapper = modelMapper;
    }


    private Transaction createAndSaveTx(Account from, Account to, TransactionRequest req) {
        Transaction tx = toEntity(req);
        System.out.println(tx);
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx = transactionRepository.save(tx);
        if (from != null) from.addOutgoingTransaction(tx);
        if (to != null) to.addIncomingTransaction(tx);
        return tx;
    }

    public TransactionResponse transfer(TransferRequest req) {
        List<Account> accounts = accountRepository.lockOrThrow(
                List.of(req.getFromAccountId(), req.getToAccountId())
        );
        Account from = accounts.get(0);
        Account to = accounts.get(1);
        BigDecimal amount = req.getAmount();

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException();

        from.modifyBalance(amount.negate());
        to.modifyBalance(amount);

        Transaction tx = createAndSaveTx(from, to, req);
        return toResponse(tx);
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest req) {
        Account to = accountRepository.lockOrThrow(req.getToAccountId());
        BigDecimal amount = req.getAmount();
        to.modifyBalance(amount);

        Transaction tx = createAndSaveTx(null, to, req);
        return toResponse(tx);
    }

    public TransactionResponse withdraw(WithdrawRequest req) {
        Account from = accountRepository.lockOrThrow(req.getFromAccountId());
        BigDecimal amount = req.getAmount();

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException();

        from.modifyBalance(amount.negate());
        Transaction tx = createAndSaveTx(from, null, req);
        return toResponse(tx);
    }

    public TransactionResponse refund(RefundRequest req) {
        Transaction original = transactionRepository.getOrThrow(req.getOriginalTransactionId());
        BigDecimal amount = original.getAmount();
        Account from = original.getFromAccount();
        Account to = original.getToAccount();

        TransactionResponse reversal = switch (original.getTransactionType()) {
            case DEPOSIT -> {
                // Reverse deposit = take money back out of 'to'
                DepositRequest depositReq = modelMapper.map(original, DepositRequest.class);
                depositReq.setToAccountId(to.getId());
                depositReq.setAmount(amount.negate());

                TransactionResponse tx = deposit(depositReq);
                yield tx;
            }
            case WITHDRAWAL -> {
                // Reverse withdrawal = put money back into 'from'
                WithdrawRequest withdrawReq = modelMapper.map(original, WithdrawRequest.class);
                withdrawReq.setFromAccountId(from.getId());
                withdrawReq.setAmount(amount.negate());

                TransactionResponse tx = withdraw(withdrawReq);
                yield tx;
            }
            case TRANSFER -> {
                // Reverse transfer = transfer back (swap sides)
                TransferRequest transferReq = modelMapper.map(original, TransferRequest.class);
                transferReq.setFromAccountId(to.getId());
                transferReq.setToAccountId(from.getId());

                TransactionResponse tx = transfer(transferReq);
                yield tx;
            }
            default -> throw new IllegalStateException("Unsupported transaction type: " + original.getTransactionType());

        };

        reversal.setAmount(amount);
        reversal.setMemo(req.getMemo());
        original.setReversed(true);
        reversal.setReversalOf(toResponse(original));

        reversal.setReversed(null);
        reversal.setToAccount(null);
        reversal.setFromAccount(null);

        return reversal;
    }

    @Override
    public Transaction toEntity(TransactionRequest req) {
        return modelMapper.map(req, Transaction.class);
    }

    @Override
    public TransactionResponse toResponse(Transaction tx) {
        TransactionResponse dto = modelMapper.map(tx, TransactionResponse.class);
        return dto;
    }
}

