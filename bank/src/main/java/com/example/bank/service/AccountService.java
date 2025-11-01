package com.example.bank.service;

import com.example.bank.dto.account.AccountDTO;
import com.example.bank.dto.account.CreateAccountDTO;
import com.example.bank.dto.transaction.TransactionResponseDTO;
import com.example.bank.entity.Account;
import com.example.bank.entity.Customer;
import com.example.bank.enums.AccountType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public AccountDTO createAccount(CreateAccountDTO accountDto) {
        // load holder by external ref (fail fast)
        Customer holder = customerRepository
            .findByCustomerRefOrThrow(accountDto.getCustomerRef());

        Account acc = toEntity(accountDto);
        holder.addAccount(acc); // maintain both sides

        // no need to flush unless rely on db generated value immediately
        Account saved = accountRepository.saveAndFlush(acc);
        return toResponse(saved);
    }

    public AccountDTO getByAccountRef(String accountRef) {
        Account acc = accountRepository.findByAccountRefOrThrow(accountRef);
        return toResponse(acc);
    }

    public AccountDTO getByAccountRefAndCustomerRef(String accountRef, String customerRef) {
        Account acc = accountRepository
            .findByAccountRefAndCustomerRefOrThrow(accountRef, customerRef);
        return toResponse(acc);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllByCustomerRef(String customerRef) {
        // it won’t cause a LazyInitializationException because fetch accounts via the repository
        // (not holder.getAccounts() after the session closes)
        Customer holder = customerRepository.findByCustomerRefOrThrow(customerRef);
        // 2. Fetch accounts belonging to this customer
        List<Account> accounts = accountRepository.findAllByAccountHolder(holder);
        return accounts.stream().map(this::toResponse).toList();
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Account toEntity(CreateAccountDTO dto) {
        return modelMapper.map(dto, Account.class);
    }

    @Override
    public AccountDTO toResponse(Account acc) {
        AccountDTO dto = modelMapper.map(acc, AccountDTO.class);
        Set<TransactionResponseDTO> transactions = Stream.concat(
                acc.getIncomingTransactions().stream()
                    .map(tx -> {
                        TransactionResponseDTO t = modelMapper.map(tx, TransactionResponseDTO.class);
                        // incoming transaction → the current account is the "to" side
                        t.setToAccountRef(null);
//                        t.setFromAccountRef(tx.getFromAccount().getAccountRef());
                        return t;
                    }),
                acc.getOutgoingTransactions().stream()
                    .map(tx -> {
                        TransactionResponseDTO t = modelMapper.map(tx, TransactionResponseDTO.class);
                        // outgoing transaction → the current account is the "from" side
                        t.setFromAccountRef(null);
//                        t.setToAccountRef(tx.getToAccount().getAccountRef());
                        return t;
                    })
        ).collect(Collectors.toSet());

        dto.setAccountHolderRef(acc.getAccountHolder().getCustomerRef());
        dto.setTransactions(transactions);
        return dto;
    }

}