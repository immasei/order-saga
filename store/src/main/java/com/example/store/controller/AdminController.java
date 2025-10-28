package com.example.store.controller;

import com.example.store.dto.account.CreateAdminDTO;
import com.example.store.dto.account.UserDTO;
import com.example.store.enums.UserRole;
import com.example.store.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated Routes: ADMIN only
 */

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<UserDTO> createAdmin(@RequestBody @Valid CreateAdminDTO adminDto) {
        UserDTO admin = userService.createUser(adminDto);
        return new ResponseEntity<>(admin, HttpStatus.CREATED);
    }

    // Get all Admins
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllAdmins() {
        List<UserDTO> admins = userService.getUsersByRole(UserRole.ADMIN);
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }

    // Get Admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getAdminById(@PathVariable UUID id) {
        UserDTO admin = userService.getUserByIdAndRole(id, UserRole.ADMIN);
        return ResponseEntity.ok(admin);
    }
}
