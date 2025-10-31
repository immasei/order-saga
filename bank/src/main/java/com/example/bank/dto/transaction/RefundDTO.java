package com.example.bank.dto.transaction;

import com.example.bank.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefundDTO extends TransactionDTO {
    // more like a reversal request for any transaction type

    { super.setTransactionType(TransactionType.REFUND); }

    @NotNull(message = "Original transaction ref is required.")
    @Size(max = 100, message = "Original transaction ref is too long")
    private String originalTransactionRef;
}
