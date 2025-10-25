package com.example.bank.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/* Example JSON:
{
    "firstName": "Bruno",
    "lastName": "Mars"
}
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerDTO {

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100, message = "First name is too long")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100, message = "Last name is too long")
    private String lastName;

}