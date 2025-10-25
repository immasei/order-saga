package com.example.bank.dto.account;

import com.example.bank.validation.ValidEnum;
import com.example.bank.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Example JSON:
{
    "accountName": "Bruno Mars",
    "accountType": "PERSONAL"
}
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountDTO {

    @NotBlank(message = "Account name cannot be blank")
    @Size(max = 200, message = "Account name is too long")
    private String accountName;

    @NotNull(message = "Account type is required")
    @ValidEnum(enumClass = AccountType.class, message = "Invalid account type provided")
    private String accountType;

}
