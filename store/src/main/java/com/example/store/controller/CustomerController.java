package com.example.store.controller;

import com.example.store.dto.account.CreateCustomerDTO;
import com.example.store.dto.account.UpdateCustomerDTO;
import com.example.store.dto.account.UserDTO;
import com.example.store.model.enums.UserRole;
import com.example.store.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated Routes
 */

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    // Get customer by ID (admin or the customer themselves)
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isAccountOwner(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO customer = userService.getUserByIdAndRole(id, UserRole.CUSTOMER);
        return ResponseEntity.ok(customer);
    }

    // Create a new user (admin only)
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> createCustomer(@RequestBody CreateCustomerDTO customerDto) {
        UserDTO customer = userService.createUser(customerDto);
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    // Update customer details (admin or the customer themselves)
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isAccountOwner(#id)")
    @PatchMapping(path = "/{id}")
    public ResponseEntity<UserDTO> updateCustomer(
        @PathVariable UUID id, @RequestBody @Valid UpdateCustomerDTO customerDto
    ) {
        UserDTO customer = userService.updateUserById(id, customerDto);
        return ResponseEntity.ok(customer);
    }

    // Delete customer by ID (admin or the customer themselves)
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isAccountOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    // Get all customers (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllCustomers() {
        List<UserDTO> customers = userService.getUsersByRole(UserRole.CUSTOMER);
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

}
