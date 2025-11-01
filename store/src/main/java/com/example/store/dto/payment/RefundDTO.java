package com.example.store.dto.payment;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class RefundDTO {

    private String originalTransactionRef;
    private String memo;
    private String idempotencyKey;

}