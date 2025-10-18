package com.deliveryco.domain.model;

public enum DeliveryJobType {
    ACKNOWLEDGE_REQUEST,
    PICKUP_ORDER,
    START_TRANSIT,
    COMPLETE_DELIVERY,
    MARK_LOST,
    CANCEL_ORDER,
    PUBLISH_OUTBOX
}

