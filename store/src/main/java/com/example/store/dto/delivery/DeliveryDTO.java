package com.example.store.dto.delivery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DeliveryDTO {
    private String externalOrderId;
    private String customerId;
    private Map<String, String> pickupLocations;
    private String dropoffAddress;
    private String contactEmail;
    private double lossRate;
    private Map<String, Map<String, Integer>> items;
}