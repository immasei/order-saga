package com.deliveryco.entity;

import com.deliveryco.domain.model.DeliveryIncidentType;
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
@Table(name = "delivery_incident")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryIncidentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_item_id", nullable = false)
    private DeliveryItemEntity deliveryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 32)
    private DeliveryIncidentType incidentType;

    @Column(name = "notes")
    private String notes;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @Version
    private long version;
}

