package com.example.bank.service;

import com.example.bank.dto.customer.CreateCustomerRequest;
import com.example.bank.dto.customer.CustomerResponse;
import com.example.bank.dto.account.AccountResponse;
import com.example.bank.entity.Customer;
import com.example.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService implements DtoMapper<Customer, CreateCustomerRequest, CustomerResponse> {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, ModelMapper modelMapper) {
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = customerRepository.save(toEntity(request));
        return toResponse(customer);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.getOrThrow(id);
        return toResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
            .stream()
            .map(customer -> toResponse(customer))
            .collect(Collectors.toList());
    }

    @Override
    public Customer toEntity(CreateCustomerRequest req) {
        return modelMapper.map(req, Customer.class);
    }

    @Override
    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse dto = modelMapper.map(customer, CustomerResponse.class);
        List<AccountResponse> accounts = customer.getAccounts().stream()
                .map(acc -> modelMapper.map(acc, AccountResponse.class))
                .toList();

        dto.setAccounts(accounts);
        return dto;
    }

}

