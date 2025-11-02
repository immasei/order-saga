package com.example.store.dto.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO returned when an admin creates a new user (ie admin or customer)
 * through the admin management endpoints.
 *
 * This response intentionally excludes any sensitive fields such as
 * passwords or tokens. It is meant to confirm that the user account
 * was successfully created.
 *
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private UUID id;
    private String username;
    private String email;
    private String role;

    // if nullable then not shown (ie ADMIN)
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

}