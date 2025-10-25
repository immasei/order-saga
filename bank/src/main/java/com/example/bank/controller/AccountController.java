package com.example.bank.controller;

import com.example.bank.dto.account.AccountDTO;
import com.example.bank.dto.account.CreateAccountDTO;
import com.example.bank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
        @PathVariable Long customerId, @RequestBody @Valid CreateAccountDTO accountDto
    ) {
        AccountDTO account = accountService.createAccount(customerId, accountDto);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(
        @PathVariable Long customerId, @PathVariable Long accountId
    ) {
        AccountDTO account = accountService.getAccountByIdAndCustomer(customerId, accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts(@PathVariable Long customerId) {
        List<AccountDTO> accounts = accountService.getAllAccountsByCustomer(customerId);
        return ResponseEntity.ok(accounts);
    }

}
