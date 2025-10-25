package com.example.bank.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.util.List;

@Data
@Builder
public class ApiError {
    // the reason for an exception handler is to map framework's internal errors to other errors (+safe messages)
    private int status;      // ie 404
    private String error;    // ie NOT_FOUND
    private String message;  // our custom message
    private List<String> subErrors;
    private String path;

    public static ApiError of(HttpStatus s, String msg, String path, List<String> sub) {
        return ApiError.builder()
                .status(s.value())
                .error(s.getReasonPhrase())
                .message(msg)
                .path(path)
                .subErrors(sub)
                .build();
    }
}
