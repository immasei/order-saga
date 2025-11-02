package com.example.store.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be within 1-50 character long")
    private String username;

//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
//    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

}
