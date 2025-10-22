package com.deliveryco.entity;

import com.deliveryco.domain.model.DeliveryJobState;
import com.deliveryco.domain.model.DeliveryJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "delivery_job")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryJobEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrderEntity deliveryOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 32)
    private DeliveryJobType jobType;

    @Column(name = "run_at", nullable = false)
    private OffsetDateTime runAt;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private DeliveryJobState state;

    @Column(name = "lock_owner")
    private String lockOwner;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private long version;
}

