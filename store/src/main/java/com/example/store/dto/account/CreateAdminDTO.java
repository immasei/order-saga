package com.example.store.dto.account;

import jakarta.validation.constraints.AssertTrue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateAdminDTO extends SignUpDTO {

    @AssertTrue(message = "Role must be ADMIN for admin creation.")
    public boolean isAdminRole() {
        return getRole() != null && getRole().equalsIgnoreCase("ADMIN");
    }

}
