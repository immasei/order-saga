package com.example.bank.exception;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ApiError error = ApiError.of(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    @ExceptionHandler({
//        ObjectOptimisticLockingFailureException.class,
//        PessimisticLockException.class,
//    })
//    public ResponseEntity<ApiError> handleLockConflict(Exception ex, HttpServletRequest req) {
//        ApiError error = ApiError.of(
//                HttpStatus.CONFLICT,
//                "The record was modified by another transaction.",
//                req.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
//    }
//
//    @ExceptionHandler(LockTimeoutException.class)
//    public ResponseEntity<ApiError> handleLockTimeout(LockTimeoutException ex, HttpServletRequest req) {
//        ApiError error = ApiError.of(
//                HttpStatus.SERVICE_UNAVAILABLE,
//                "The operation timed out while waiting for a database lock. Please retry later.",
//                req.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
//    }

    // ----- Fallback -----
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
//        // log privately
//        // log.error("Unexpected error on {} {}", req.getMethod(), req.getRequestURI(), ex);
//        //Log the real exception server-side + send safe message to client
//        ApiError error = ApiError.of(
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                "Unexpected server error.",
//                req.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//    }


}














