package com.example.bank.controller;

import com.example.bank.dto.transaction.*;
import com.example.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(@RequestBody @Valid TransferDTO transferDto) {
        TransactionResponseDTO transaction = transactionService.process(transferDto);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(@RequestBody @Valid DepositDTO depositDto) {
        TransactionResponseDTO transaction = transactionService.process(depositDto);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(@RequestBody @Valid WithdrawDTO withdrawDto) {
        TransactionResponseDTO transaction = transactionService.process(withdrawDto);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponseDTO> refund(@RequestBody @Valid RefundDTO refundDto) {
        TransactionResponseDTO transaction = transactionService.process(refundDto);
        return ResponseEntity.ok(transaction);
    }
}
