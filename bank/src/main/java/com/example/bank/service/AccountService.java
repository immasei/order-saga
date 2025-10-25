package com.example.bank.service;

import com.example.bank.dto.account.AccountDTO;
import com.example.bank.dto.account.CreateAccountDTO;
import com.example.bank.dto.transaction.TransactionResponseDTO;
import com.example.bank.entity.Account;
import com.example.bank.entity.Customer;
import com.example.bank.enums.AccountType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AccountService implements DtoMapper<Account, CreateAccountDTO, AccountDTO> {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public AccountDTO createAccount(Long customerId, CreateAccountDTO accountDto) {
        Account newAccount = toEntity(accountDto);
        Customer customer = customerRepository.getOrThrow(customerId);
        customer.addAccount(newAccount);

        Account saved = accountRepository.saveAndFlush(newAccount);
        return toResponse(saved);
    }

    public AccountDTO getAccountByIdAndCustomer(Long customerId, Long accountId) {
        Account account = accountRepository.getOrThrow(customerId, accountId);
        return toResponse(account);
    }

    public List<AccountDTO> getAllAccountsByCustomer(Long customerId) {
        return accountRepository.findAllByCustomer_Id(customerId)
                .stream()
                .map(acc -> toResponse(acc))
                .collect(Collectors.toList());
    }

    @Override
    public Account toEntity(CreateAccountDTO dto) {
        Account acc = modelMapper.map(dto, Account.class);
        acc.setAccountType(AccountType.valueOf(
            dto.getAccountType().toUpperCase()
        ));
        return acc;
    }

    @Override
    public AccountDTO toResponse(Account acc) {
        AccountDTO dto = modelMapper.map(acc, AccountDTO.class);

        Set<TransactionResponseDTO> transactions = Stream.concat(
                acc.getIncomingTransactions().stream()
                    .map(tx -> {
                        TransactionResponseDTO t = modelMapper.map(tx, TransactionResponseDTO.class);
                        // incoming transaction → the current account is the "to" side
                        t.setToAccount(null);
                        return t;
                    }),
                acc.getOutgoingTransactions().stream()
                    .map(tx -> {
                        TransactionResponseDTO t = modelMapper.map(tx, TransactionResponseDTO.class);
                        // outgoing transaction → the current account is the "from" side
                        t.setFromAccount(null);
                        return t;
                    })
        ).collect(Collectors.toSet());

        dto.setTransactions(transactions);
        return dto;
    }

}