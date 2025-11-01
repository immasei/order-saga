package com.example.bank.dto.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String transactionRef;
    private BigDecimal amount;
    private String memo;
    private String fromAccountRef;
    private String toAccountRef;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private TransactionResponseDTO reversalOf;
    private Boolean reversed;
}
