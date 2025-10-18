package com.example.bank.dto.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/* Example JSON:
{
    "originalTransactionId": "2"
}
*/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefundRequest extends TransactionRequest {
    // more like a reversal request for any transaction type

    @NotNull(message = "Original transaction id is required.")
    private Long originalTransactionId;
//    private String idempotencyKey;

}
