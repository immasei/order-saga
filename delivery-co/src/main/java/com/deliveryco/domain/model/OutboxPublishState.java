package com.deliveryco.domain.model;

public enum OutboxPublishState {
    PENDING,
    IN_FLIGHT,
    PUBLISHED,
    FAILED,
    DEAD_LETTERED
}

