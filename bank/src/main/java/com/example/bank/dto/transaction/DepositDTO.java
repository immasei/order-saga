package com.example.bank.dto.transaction;

import com.example.bank.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DepositDTO extends TransactionDTO {

    { super.setTransactionType(TransactionType.DEPOSIT); }

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull(message = "Destination account ref is required.")
    @Size(max = 100, message = "Destination account ref is too long")
    private String toAccountRef;

}