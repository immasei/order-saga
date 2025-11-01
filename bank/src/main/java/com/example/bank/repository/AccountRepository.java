package com.example.bank.repository;

import com.example.bank.entity.Account;
import com.example.bank.entity.Customer;
import com.example.bank.exception.ResourceNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountRef(String accountRef);
    Optional<Account> findByAccountRefAndAccountHolder_CustomerRef(String accountRef, String customerRef);
    List<Account> findAllByAccountHolder(Customer customer);
    List<Account> findAllByAccountHolder_CustomerRef(String customerRef);

    default Account findByAccountRefOrThrow(String accountRef) {
        return findByAccountRef(accountRef)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountRef));
    }

    default Account findByAccountRefAndCustomerRefOrThrow(String accountRef, String customerRef) {
        return findByAccountRefAndAccountHolder_CustomerRef(accountRef, customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountRef + " for customer " + customerRef));
    }

    // lock for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")) // NOWAIT; use "-2" for SKIP LOCKED if your JPA provider supports it
    @Query("select a from Account a where a.accountRef in :refs")
    List<Account> lockAllByAccountRefs(@Param("refs") List<String> refs);

    // lock for update or throw
    default List<Account> lockOrThrowByRefsInDeterministicOrder(List<String> inputRefs) {
        var dedup = inputRefs.stream().distinct().toList();
        if (dedup.size() != 2) {
            throw new IllegalArgumentException("Exactly 2 distinct account refs required.");
        }
        // Always lock in deterministic order to avoid deadlocks
        var sorted = dedup.stream().sorted().toList();

        List<Account> locked = lockAllByAccountRefs(sorted);
        if (locked.size() != 2) {
            throw new com.example.bank.exception.ResourceNotFoundException(
                    "One or more accounts not found: " + dedup
            );
        }
        // Return in the SAME order as 'sorted'
        locked.sort(java.util.Comparator.comparing(Account::getAccountRef));
        return locked;
    }

    // lock for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")) // NOWAIT (fail fast)
    @Query("select a from Account a where a.accountRef = :ref")
    Optional<Account> lockByAccountRef(@Param("ref") String ref);

    // lock for update or throw
    default Account lockOrThrowByRef(String ref) {
        return lockByAccountRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + ref));
    }

}