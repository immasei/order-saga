package com.example.store.controller;

import com.example.store.model.UserAccount;
import com.example.store.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {

    @Autowired
    private UserAccountService userService;

    @GetMapping("/{userId}")
    public Mono<UserAccount> getUser(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }


    @PostMapping
    public Mono<ResponseEntity<UserAccount>> createUser(@RequestBody UserAccount userAccount) {
        return userService.createUser(userAccount)
                .map(createdUser -> new ResponseEntity<>(createdUser, HttpStatus.CREATED));
    }
}
