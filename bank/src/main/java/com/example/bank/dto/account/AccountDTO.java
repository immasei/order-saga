package com.example.bank.dto.account;

import com.example.bank.dto.transaction.TransactionResponseDTO;
import com.example.bank.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {

    private Long id;
    private String accountName;
    private Double balance;
    private AccountType accountType;
    private Long customerId;
    private Set<TransactionResponseDTO> transactions;

}