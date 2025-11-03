package com.example.store.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

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

    @JsonIgnore
    private String refreshToken; // already attached in cookie

    public LoginResponseDTO(UserDTO userDto, String accessToken, String refreshToken) {
        this.setId(userDto.getId());
        this.setUsername(userDto.getUsername());
        this.setEmail(userDto.getEmail());
        this.setRole(userDto.getRole());
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}