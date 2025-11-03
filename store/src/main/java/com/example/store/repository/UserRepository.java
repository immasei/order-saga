package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.User;
import com.example.store.enums.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndRole(UUID id, UserRole role);
    List<User> findAllByRole(UserRole role);
    int countByRole(UserRole userRole);

    default User getOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    default User getOrThrow(UUID id, UserRole role) {
        return findByIdAndRole(id, role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id + " and role: " + role));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    // Row lock + throw if missing
    default User getForUpdateOrThrow(UUID id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

}
