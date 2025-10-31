package com.example.bank.service;

import com.example.bank.dto.customer.CreateCustomerDTO;
import com.example.bank.dto.customer.CustomerDTO;
import com.example.bank.dto.account.AccountDTO;
import com.example.bank.entity.Customer;
import com.example.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService implements DtoMapper<Customer, CreateCustomerDTO, CustomerDTO> {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public CustomerDTO createCustomer(CreateCustomerDTO customerDto) {
        Customer customer = customerRepository.save(toEntity(customerDto));
        return toResponse(customer);
    }

    public CustomerDTO getCustomerByRef(String ref) {
        Customer customer = customerRepository.findByCustomerRefOrThrow(ref);
        return toResponse(customer);
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Customer toEntity(CreateCustomerDTO dto) {
        return modelMapper.map(dto, Customer.class);
    }

    @Override
    public CustomerDTO toResponse(Customer cli) {
        CustomerDTO dto = modelMapper.map(cli, CustomerDTO.class);
        List<AccountDTO> accounts = cli.getAccounts().stream()
                .map(acc -> {
                    AccountDTO accountDto = modelMapper.map(acc, AccountDTO.class);
                    accountDto.setAccountHolderRef(null);
                    return accountDto;
                })
                .toList();

        dto.setAccounts(accounts);
        return dto;
    }

}

