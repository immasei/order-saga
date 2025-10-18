package com.example.bank.controller;

import com.example.bank.dto.transaction.*;
import com.example.bank.enums.TransactionType;
import com.example.bank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransferRequest req) {
        req.setMemo("Transfer.");
        req.setTransactionType(TransactionType.TRANSFER);
        TransactionResponse transaction = transactionService.transfer(req);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody @Valid DepositRequest req) {
        req.setMemo("Deposit.");
        req.setTransactionType(TransactionType.DEPOSIT);
        TransactionResponse transaction = transactionService.deposit(req);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody @Valid WithdrawRequest req) {
        req.setMemo("Withdraw.");
        req.setTransactionType(TransactionType.WITHDRAWAL);
        TransactionResponse transaction = transactionService.withdraw(req);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(@RequestBody @Valid RefundRequest req) {
        req.setMemo("Refund.");
        req.setTransactionType(TransactionType.REFUND);
        TransactionResponse transaction = transactionService.refund(req);

        return ResponseEntity.ok(transaction);
    }
}
