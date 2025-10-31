package com.example.bank.exception;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // --- tiny helper to keep handlers one-liners
    private ResponseEntity<ApiError> respond(HttpStatus status, String msg,
                                             HttpServletRequest req, List<String> sub) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status, msg, req.getRequestURI(), sub));
    }

    private String deepestMessage(Throwable t) {
        String last = null;
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null) last = c.getMessage();
        }
        return last;
    }

    // 409 conflict ie duplicated refunded/ idempotency key exists
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // 400 (business logic: insufficient funds)
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> insufficientBalance(InsufficientBalanceException ex,
                                                        HttpServletRequest req) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient balance.", req, List.of(ex.getMessage()));
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    // 400 (DTO @Valid on @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> badRequest(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> sub = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        return respond(HttpStatus.BAD_REQUEST, "Validation failed", req, sub);
    }

    // 400 (@Validated on params or programmatic Validator)
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<ApiError> badRequest2(jakarta.validation.ConstraintViolationException ex, HttpServletRequest req) {
//        List<String> sub = ex.getConstraintViolations().stream()
//                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
//                .collect(Collectors.toList());
//        return respond(HttpStatus.BAD_REQUEST, "Validation failed", req, sub);
//    }

//    // 500 fallback
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
//        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null);
//    }
}














