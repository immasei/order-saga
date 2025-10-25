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

    // 400 (business logic: insufficient funds)
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> insufficientBalance(InsufficientBalanceException ex,
                                                        HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "Transaction failed", req, List.of(ex.getMessage()));
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

    @ExceptionHandler(PessimisticLockException.class)
    public ResponseEntity<ApiError> pessimisticLocked(PessimisticLockException ex,
                                                      HttpServletRequest req) {
        String mostSpecific = deepestMessage(ex);
        return respond(HttpStatus.CONFLICT, "Resource is locked (pessimistic lock)", req,
                mostSpecific == null ? null : List.of(mostSpecific));
    }

    @ExceptionHandler(LockTimeoutException.class)
    public ResponseEntity<ApiError> lockTimeout(LockTimeoutException ex,
                                                HttpServletRequest req) {
        String mostSpecific = deepestMessage(ex);
        return respond(HttpStatus.CONFLICT, "Lock wait timed out – please retry", req,
                mostSpecific == null ? null : List.of(mostSpecific));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> optimisticConflict(ObjectOptimisticLockingFailureException ex,
                                                       HttpServletRequest req) {
        // Prefer explicit entity + id if available; otherwise fall back to message
        var sub = new java.util.ArrayList<String>();
        if (ex.getPersistentClassName() != null) sub.add("entity: " + ex.getPersistentClassName());
        if (ex.getIdentifier() != null)        sub.add("id: " + ex.getIdentifier());
        if (sub.isEmpty() && ex.getMessage() != null) sub.add(ex.getMessage());

        return respond(HttpStatus.CONFLICT, "Edit conflict (optimistic locking failed)", req,
                sub.isEmpty() ? null : sub);
    }

    // 500 fallback
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
    //    log here
//        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null);
//    }
}














