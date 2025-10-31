package com.example.bank.controller;

import com.example.bank.dto.account.AccountDTO;
import com.example.bank.dto.customer.CreateCustomerDTO;
import com.example.bank.dto.customer.CustomerDTO;
import com.example.bank.service.AccountService;
import com.example.bank.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
        @RequestBody @Valid CreateCustomerDTO customerDto
    ) {
        CustomerDTO customer = customerService.createCustomer(customerDto);
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    @GetMapping("/{customerRef}")
    public ResponseEntity<CustomerDTO> getCustomerByRef(@PathVariable String customerRef) {
        CustomerDTO customer = customerService.getCustomerByRef(customerRef);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("{customerRef}/accounts/{accountRef}")
    public ResponseEntity<AccountDTO> getByAccountRefAndCustomerRef(
        @PathVariable String customerRef, @PathVariable String accountRef
    ) {
        AccountDTO account = accountService.getByAccountRefAndCustomerRef(accountRef, customerRef);
        return ResponseEntity.ok(account);
    }

    @GetMapping("{customerRef}/accounts")
    public ResponseEntity<List<AccountDTO>> getAllByCustomerRef(
        @PathVariable String customerRef
    ) {
        List<AccountDTO> accounts = accountService.getAllByCustomerRef(customerRef);
        return ResponseEntity.ok(accounts);
    }

}
