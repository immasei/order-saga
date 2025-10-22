package com.example.store.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
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

    // 401 (auth errors, JWT errors)
    @ExceptionHandler({AuthenticationException.class, JwtException.class})
    public ResponseEntity<ApiError> unauthorized(Exception ex, HttpServletRequest req) {
        // message can go to subErrors to avoid leaking internals in the main message
        return respond(HttpStatus.UNAUTHORIZED, "Unauthorized", req,
                ex.getMessage() == null ? null : List.of(ex.getMessage()));
    }

    // 409/400 (DB constraint violations)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        if (isUkUsersEmail(ex)) {
            return respond(HttpStatus.CONFLICT, "Email is already in use", req, null);
        }
        // Fallback: generic constraint error with most specific message as subError
        String mostSpecific = ex.getMostSpecificCause() == null ? null : ex.getMostSpecificCause().getMessage();
        return respond(HttpStatus.BAD_REQUEST, "Constraint violation", req,
                mostSpecific == null ? null : List.of(mostSpecific));
    }

    private boolean isUkUsersEmail(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String msg = c.getMessage();
            if (msg != null && msg.contains("uk_users_email")) return true;
        }
        return false;
    }

    // 500 fallback
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
//        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null);
//    }

}

