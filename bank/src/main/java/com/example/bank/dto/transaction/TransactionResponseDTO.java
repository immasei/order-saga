package com.example.bank.dto.transaction;

import com.example.bank.dto.account.AccountDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class TransactionResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String memo;
    private AccountDTO fromAccount;
    private AccountDTO toAccount;

    @JsonIgnore
    private LocalDateTime time;

    private TransactionResponseDTO reversalOf;
    private Boolean reversed;
}
