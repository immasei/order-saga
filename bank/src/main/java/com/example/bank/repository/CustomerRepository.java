package com.example.bank.repository;

import com.example.bank.entity.Customer;
import com.example.bank.exception.ResourceNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerRef(String customerRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findByIdForUpdate(@Param("id") long id);

    default Customer getOrThrow(long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    default Customer getForUpdateOrThrow(long id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    default Customer findByCustomerRefOrThrow(String customerRef) {
        return findByCustomerRef(customerRef)
                .orElseThrow(() -> new com.example.bank.exception.ResourceNotFoundException(
                        "Customer not found with ref: " + customerRef
                ));
    }



}

