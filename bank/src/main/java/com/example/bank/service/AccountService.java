package com.example.bank.service;

import com.example.bank.dto.account.AccountResponse;
import com.example.bank.dto.account.CreateAccountRequest;
import com.example.bank.dto.transaction.TransactionResponse;
import com.example.bank.entity.Account;
import com.example.bank.entity.Customer;
import com.example.bank.enums.AccountType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AccountService implements DtoMapper<Account, CreateAccountRequest, AccountResponse> {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
                          ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public AccountResponse createAccount(Long customerId, CreateAccountRequest req) {
        Account account = toEntity(req);
        Customer customer = customerRepository.getOrThrow(customerId);
        customer.addAccount(account);
        account = accountRepository.save(account);

        AccountResponse accountDTO = toResponse(account);
        return accountDTO;
    }

    public AccountResponse getAccountByIdAndCustomer(Long customerId, Long accountId) {
        Account account = accountRepository.getOrThrow(customerId, accountId);
        return toResponse(account);
    }

    public List<AccountResponse> getAllAccountsByCustomer(Long customerId) {
        List<Account> accounts = accountRepository.findAllByCustomer_Id(customerId);

        return accounts
                .stream()
                .map(acc -> toResponse(acc))
                .collect(Collectors.toList());
    }

    @Override
    public Account toEntity(CreateAccountRequest req) {
        Account acc = modelMapper.map(req, Account.class);
        acc.setAccountType(AccountType.valueOf(
                req.getAccountType().toUpperCase()
        ));
        return acc;
    }

    @Override
    public AccountResponse toResponse(Account acc) {
        AccountResponse dto = modelMapper.map(acc, AccountResponse.class);

        Set<TransactionResponse> transactions = Stream.concat(
                acc.getIncomingTransactions().stream()
                    .map(tx -> {
                        TransactionResponse t = modelMapper.map(tx, TransactionResponse.class);
                        // incoming transaction → the current account is the "to" side
                        t.setToAccount(null);
                        return t;
                    }),
                acc.getOutgoingTransactions().stream()
                    .map(tx -> {
                        TransactionResponse t = modelMapper.map(tx, TransactionResponse.class);
                        // outgoing transaction → the current account is the "from" side
                        t.setFromAccount(null);
                        return t;
                    })
        ).collect(Collectors.toSet());

        dto.setTransactions(transactions);
        return dto;
    }

}