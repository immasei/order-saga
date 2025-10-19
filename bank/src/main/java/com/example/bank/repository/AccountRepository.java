package com.example.bank.repository;

import com.example.bank.entity.Account;
import com.example.bank.entity.Customer;
import com.example.bank.exception.ResourceNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Spring Data JPA will automatically generate the implementation.
    // Find more about it at https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    Optional<Account> findByIdAndCustomer(long id, Customer customer);
    Optional<Account> findByIdAndCustomer_Id(Long accountId, Long customerId);
    List<Account> findAllByCustomer_Id(Long customerId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") long id);

    @Query("SELECT a FROM Account a WHERE a.id IN :ids ORDER BY a.id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Account> findByIdsForUpdateOrdered(@Param("ids") List<Long> ids);

    default Account getOrThrow(long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    default Account getOrThrow(long customerId, long accountId) {
        return findByIdAndCustomer_Id(accountId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account id " + accountId + " not found for customer id " + customerId));
    }

    default Account lockOrThrow(long id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    default List<Account> lockOrThrow(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("No account ids provided");
        }

        // ensure deterministic order for deadlock prevention
        List<Long> sortedIds = ids.stream().sorted().toList();
        List<Account> locked = findByIdsForUpdateOrdered(sortedIds);

        if (locked.size() != sortedIds.size()) {
            throw new ResourceNotFoundException(
                "Some accounts not found: expected " + sortedIds.size() + " but found " + locked.size()
            );
        }

        // restore original order for caller readability
        Map<Long, Account> map = locked.stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        return ids.stream().map(map::get).toList();
    }
}