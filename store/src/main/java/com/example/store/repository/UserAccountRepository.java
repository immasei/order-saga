package com.example.store.repository;

import com.example.store.model.Product;
import com.example.store.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    // You can add methods here like:
    Optional<UserAccount> findByEmail(String email);
}
