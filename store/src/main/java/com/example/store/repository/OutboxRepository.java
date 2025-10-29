package com.example.store.repository;

import com.example.store.model.Outbox;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query("""
    select o from Outbox o
    where (o.status = 'PENDING' or o.status = 'FAILED')
    order by o.createdAt
    """)
    Page<Outbox> findPending(Pageable pageable);

    @Modifying
    @Query("""
    update Outbox o
    set o.status = 'IN_PROGRESS'
    where o.id = :id and o.status = 'PENDING'
    """)
    int tryClaim(@Param("id") UUID id);

    @Modifying
    @Query("""
    update Outbox o 
    set o.status='SENT', o.attempts = o.attempts + 1
    where o.id=:id
    """)
    void markSent(@Param("id") UUID id);

    @Modifying
    @Query("""
    update Outbox o
    set o.status='FAILED', o.attempts = o.attempts + 1
    where o.id = :id
    """)
    void markFailed(@Param("id") UUID id);
}

