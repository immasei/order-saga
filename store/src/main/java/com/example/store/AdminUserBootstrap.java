package com.example.store;

import com.example.store.model.Admin;
import com.example.store.model.User;
import com.example.store.enums.UserRole;
import com.example.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AdminUserBootstrap implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Value("${default.admin.username}")
    private String username;

    @Value("${default.admin.email}")
    private String email;

    @Value("${default.admin.password:}")
    private String password;

    @Override public void run(String... args) {
        if (repo.countByRole(UserRole.ADMIN) == 0 &&
                !username.isBlank() &&
                !password.isBlank()) {
            User admin = new Admin();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(encoder.encode(password));
            admin.setRole(UserRole.ADMIN);
            repo.save(admin);
        }
    }
}