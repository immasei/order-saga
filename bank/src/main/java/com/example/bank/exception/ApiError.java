package com.example.bank.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

@Data
@Builder
public class ApiError {
    // match spring default api error, so dont need to handle self-made errors in global handler
    // the reason for an exception handler is to map framework's internal errors to other errors (+safe messages)

    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;

    public static ApiError of(HttpStatus status, String message, String path) {
        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
