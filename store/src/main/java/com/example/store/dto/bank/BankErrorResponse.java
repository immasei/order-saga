package com.example.store.dto.bank;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BankErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

}

