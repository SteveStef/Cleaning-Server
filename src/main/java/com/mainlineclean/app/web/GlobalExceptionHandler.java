package com.mainlineclean.app.web;

import com.mainlineclean.app.exception.AppointmentException;
import com.mainlineclean.app.exception.AvailabilityException;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    public record ErrorResponse(String code, String message) {}

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> onEmailError(EmailException e) {
        log.error("EmailService failed: {}", e.getMessage());
        var body = new ErrorResponse("EMAIL_FAILED", e.getMessage());
        return ResponseEntity.status(500).body(body);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> onPaymentError(Exception e) {
        log.error("Payment error: {}", e.getMessage());
        var body = new ErrorResponse("PAYMENT_ERROR", e.getMessage());
        return ResponseEntity.status(500).body(body);
    }

    @ExceptionHandler(AppointmentException.class)
    public ResponseEntity<ErrorResponse> onAppointmentError(Exception e) {
        log.error("Appointment Error: {}", e.getMessage());
        var body = new ErrorResponse("APPOINTMENT_ERROR", e.getMessage());
        return ResponseEntity.status(500).body(body);
    }

    @ExceptionHandler(AvailabilityException.class)
    public ResponseEntity<ErrorResponse> onAvailabilityError(Exception e) {
        log.error("Availability Error: {}", e.getMessage());
        var body = new ErrorResponse("AVAILABILTY_ERROR", e.getMessage());
        return ResponseEntity.status(500).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> onIllegalArgumentException(Exception e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        var body = new ErrorResponse("ILLEGAL_ARGUMENT", e.getMessage());
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> onDataIntegrityViolationException(Exception e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        var body = new ErrorResponse("DATA_INTEGRITY_VIOLATION", e.getMessage());
        return ResponseEntity.status(400).body(body);
    }
}
