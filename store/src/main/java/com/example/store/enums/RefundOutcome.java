package com.example.store.enums;

public enum RefundOutcome {
    /**
     * Refund was processed successfully by the payment provider.
     */
    SUCCESS,

    /**
     * Refund was already processed before (idempotent case).
     */
    DUPLICATE,

    /**
     * Refund request is valid but there was no captured payment
     * (e.g., payment never succeeded or was voided).
     */
    NOOP_ORPHAN,

    /**
     * Refund request was rejected by the provider or disallowed by state
     * (e.g., transaction already settled, refund window expired).
     */
    REJECTED_NOT_ALLOWED,

    /**
     * Refund attempt failed due to a technical or external error (e.g. timeout).
     * Typically retried or escalated.
     */
    FAILED
}
