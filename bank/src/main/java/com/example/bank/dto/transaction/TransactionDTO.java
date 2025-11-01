package com.example.bank.dto.transaction;

import com.example.bank.enums.TransactionType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private TransactionType transactionType;
    private String memo;
    private String idempotencyKey;

    public String getMemo() {
        return (memo == null) ? "" : memo;
    }

}






