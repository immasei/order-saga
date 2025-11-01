package com.example.store.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Returned after successful login or signup through public endpoints
 * Contains the tokens and minimal user info required for the client
 * to start an authenticated session.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO extends UserDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;

    public LoginResponseDTO(UserDTO userDto, String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = userDto.getEmail();
        this.role = userDto.getRole();
    }
}