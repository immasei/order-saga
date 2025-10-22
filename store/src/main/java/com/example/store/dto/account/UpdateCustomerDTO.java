package com.example.store.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Only allows non-sensitive fields
 */

@Getter
@NoArgsConstructor
public class UpdateCustomerDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}
