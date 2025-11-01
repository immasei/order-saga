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
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody @Valid CreateAccountDTO accountDto) {
        AccountDTO account = accountService.createAccount(accountDto);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/{accountRef}")
    public ResponseEntity<AccountDTO> getAccountByRef(@PathVariable String accountRef) {
        AccountDTO account = accountService.getByAccountRef(accountRef);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

}
