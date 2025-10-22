package com.deliveryco.web;

import com.deliveryco.domain.model.DeliveryRequest;
import com.deliveryco.domain.model.DeliveryRequestItem;
import com.deliveryco.domain.service.OrderLifecycleService;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.web.dto.DeliveryRequestDto;
import com.deliveryco.web.dto.DeliveryResponseDto;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryRequestController {

    private final OrderLifecycleService orderLifecycleService;

    public DeliveryRequestController(OrderLifecycleService orderLifecycleService) {
        this.orderLifecycleService = orderLifecycleService;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponseDto> createDelivery(@Valid @RequestBody DeliveryRequestDto requestDto) {
        DeliveryRequest request = new DeliveryRequest(
                requestDto.externalOrderId(),
                requestDto.customerId(),
                requestDto.pickupWarehouseId(),
                requestDto.pickupAddress(),
                requestDto.dropoffAddress(),
                requestDto.contactEmail(),
                requestDto.lossRate(),
                mapItems(requestDto.items())
        );

        DeliveryOrderEntity order = orderLifecycleService.registerDeliveryRequest(request);
        DeliveryResponseDto response = new DeliveryResponseDto(
                order.getId(),
                order.getExternalOrderId(),
                order.getCurrentStatus(),
                order.getRequestedAt()
        );

        return ResponseEntity.created(URI.create("/api/deliveries/" + order.getId())).body(response);
    }

    private List<DeliveryRequestItem> mapItems(List<DeliveryRequestDto.DeliveryRequestItemDto> items) {
        return items.stream()
                .map(item -> new DeliveryRequestItem(item.sku(), item.description(), item.quantity()))
                .toList();
    }
}

