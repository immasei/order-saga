package com.example.store.dto.account;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateCustomerDTO extends SignUpDTO {

    @NotBlank(message = "Phone number is required.")
    private String phone;

    @NotBlank(message = "Address is required.")
    @Size(max = 255, message = "Address must not exceed 255 characters.")
    private String address;

    // optional
    @Size(max = 40, message = "Bank account is too long.")
    private String bankAccountRef;

    @AssertTrue(message = "Role must be CUSTOMER for customer creation.")
    public boolean isCustomerRole() {
        return getRole() != null && getRole().equalsIgnoreCase("CUSTOMER");
    }

}