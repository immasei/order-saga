package com.deliveryco.domain.service.support;

import com.deliveryco.domain.model.DeliveryIncidentType;
import com.deliveryco.domain.model.DeliveryItemStatus;
import com.deliveryco.entity.DeliveryIncidentEntity;
import com.deliveryco.entity.DeliveryItemEntity;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.repository.DeliveryIncidentRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LossSimulationService {

    private final DeliveryIncidentRepository incidentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public boolean shouldLosePackage(double lossRate) {
        return secureRandom.nextDouble() < lossRate;
    }

    public void markItemsLost(DeliveryOrderEntity order, DeliveryIncidentType type, String notes) {
        OffsetDateTime now = OffsetDateTime.now();
        List<DeliveryItemEntity> items = order.getItems();
        for (DeliveryItemEntity item : items) {
            item.setFulfillmentStatus(DeliveryItemStatus.LOST);
            DeliveryIncidentEntity incident = DeliveryIncidentEntity.builder()
                    .id(UUID.randomUUID())
                    .deliveryItem(item)
                    .incidentType(type)
                    .notes(notes)
                    .detectedAt(now)
                    .resolved(false)
                    .build();
            incidentRepository.save(incident);
        }
    }
}
