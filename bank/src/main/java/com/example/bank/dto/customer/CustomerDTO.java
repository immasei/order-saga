package com.example.bank.dto.customer;

import com.example.bank.dto.account.AccountDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDTO {

    private Long id;
    private String customerRef;
    private String firstName;
    private String lastName;
    private Collection<AccountDTO> accounts;

}