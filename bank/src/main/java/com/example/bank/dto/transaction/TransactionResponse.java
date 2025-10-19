package com.example.bank.dto.transaction;

import com.example.bank.dto.account.AccountResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String memo;
    private AccountResponse fromAccount;
    private AccountResponse toAccount;
    private LocalDateTime time;

    private TransactionResponse reversalOf;
    private Boolean reversed;
}
