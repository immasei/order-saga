package com.example.store.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {

    private String originalTransactionRef;
    private String memo;
    private String idempotencyKey;

}