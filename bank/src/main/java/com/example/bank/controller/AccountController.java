package com.example.bank.controller;

import com.example.bank.dto.account.AccountResponse;
import com.example.bank.dto.account.CreateAccountRequest;
import com.example.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long customerId,
            @RequestBody @Valid CreateAccountRequest req
    ) {
        AccountResponse account = accountService.createAccount(customerId, req);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long customerId,
            @PathVariable Long accountId
    ) {
        AccountResponse account = accountService.getAccountByIdAndCustomer(customerId, accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @PathVariable Long customerId
    ) {
        List<AccountResponse> accounts = accountService.getAllAccountsByCustomer(customerId);
        return ResponseEntity.ok(accounts);
    }

}
