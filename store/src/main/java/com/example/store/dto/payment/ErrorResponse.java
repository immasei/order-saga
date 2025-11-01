package com.example.store.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

}

