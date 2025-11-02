package com.example.store.service;

import com.example.store.dto.account.UserDTO;
import com.example.store.model.Customer;
import com.example.store.repository.CustomerRepository;
import com.example.store.security.CustomerSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Optional<Customer> getCurrentCustomer() {
        // This method doesn't have access to Authentication
        return Optional.empty(); // Always returns empty
    }

    // Add this method that accepts Authentication
    public Optional<Customer> getCurrentCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();
        System.out.println("Looking up customer with email: " + email);
        return customerRepository.findByEmail(email);
    }
}