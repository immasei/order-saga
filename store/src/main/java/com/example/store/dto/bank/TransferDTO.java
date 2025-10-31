package com.example.store.dto.bank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferDTO {

    private String fromAccountRef;
    private String toAccountRef;
    private BigDecimal amount;
    private String memo;

}