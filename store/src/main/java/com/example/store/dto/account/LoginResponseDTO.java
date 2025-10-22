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
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO extends UserDTO {

    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    public LoginResponseDTO(UserDTO userDTO, String accessToken, String refreshToken) {
        super(userDTO.getId(), userDTO.getEmail(), userDTO.getRole(), null, null, null, null);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
