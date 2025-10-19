package com.example.bank.dto.transaction;

import com.example.bank.enums.TransactionType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    private TransactionType transactionType;
    private String memo;

}






