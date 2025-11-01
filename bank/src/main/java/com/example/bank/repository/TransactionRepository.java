package com.example.bank.repository;

import com.example.bank.entity.Transaction;
import com.example.bank.exception.ResourceNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")) // NOWAIT
    @Query("select t from Transaction t where t.transactionRef = :ref")
    Optional<Transaction> lockByRef(@Param("ref") String ref);

    default Transaction lockOrThrowByRef(String ref) {
        return lockByRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + ref));
    }

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);;
}