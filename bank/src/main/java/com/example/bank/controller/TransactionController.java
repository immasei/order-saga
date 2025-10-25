package com.example.bank.controller;

import com.example.bank.dto.transaction.*;
import com.example.bank.enums.TransactionType;
import com.example.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(@RequestBody @Valid TransferDTO transferDto) {
        transferDto.setMemo("Transfer.");
        transferDto.setTransactionType(TransactionType.TRANSFER);
        TransactionResponseDTO transaction = transactionService.transfer(transferDto);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(@RequestBody @Valid DepositDTO depositDto) {
        depositDto.setMemo("Deposit.");
        depositDto.setTransactionType(TransactionType.DEPOSIT);
        TransactionResponseDTO transaction = transactionService.deposit(depositDto);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(@RequestBody @Valid WithdrawDTO withdrawDto) {
        withdrawDto.setMemo("Withdraw.");
        withdrawDto.setTransactionType(TransactionType.WITHDRAWAL);
        TransactionResponseDTO transaction = transactionService.withdraw(withdrawDto);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponseDTO> refund(@RequestBody @Valid RefundDTO refundDto) {
        refundDto.setMemo("Refund.");
        refundDto.setTransactionType(TransactionType.REFUND);
        TransactionResponseDTO transaction = transactionService.refund(refundDto);

        return ResponseEntity.ok(transaction);
    }
}
