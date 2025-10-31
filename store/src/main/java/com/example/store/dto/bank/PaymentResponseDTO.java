package com.example.store.dto.bank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String memo;
    private AccountDTO fromAccount;
    private AccountDTO toAccount;

    private PaymentResponseDTO reversalOf;
    private Boolean reversed;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class AccountDTO {
        private Long id;
        private String accountName;
    }
}
