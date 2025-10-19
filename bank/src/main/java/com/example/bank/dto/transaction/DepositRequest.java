package com.example.bank.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

/* Example JSON:
{
    "amount": "10.0",
    "toAccountId": "1"
}
*/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DepositRequest extends TransactionRequest {

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull(message = "Destination account ID is required.")
    private Long toAccountId;

}