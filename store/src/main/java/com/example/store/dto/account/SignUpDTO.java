package com.example.store.dto.account;

import com.example.store.enums.UserRole;
import com.example.store.validation.ValidEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpDTO {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "User role is required")
    @ValidEnum(enumClass = UserRole.class, message = "Invalid user role provided")
    private String role; // allow mixed case

}
