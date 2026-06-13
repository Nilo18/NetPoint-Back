package com.netpoint.main.exceptions;

import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/*
* კაროჩე ეს გლობალური ექსეფშენ ჰენდლერია
* თუ რამე სპეციალური ექსეფშენის დაწერა დაგჭირდება, ჯერ მიხვალ შესაბამის ექსეფშენის კლასს შექმნი,
* მერე მოხვალ აქ და ჩაამატებ, როგორც ქვემოთაა ნაჩვენები
*/
@RestControllerAdvice
@Log
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMissingFields(MethodArgumentNotValidException ex) {
        // This will join missing field error messages
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRole(InvalidRoleException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOtp(InvalidOtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOtpNotFound(OtpNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<Map<String, String>> handleOtpNotFound(OtpExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleOtpNotFound(MailAuthenticationException ex) {
        log.info("Full reason: " + ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInvitationNotFound(InvitationTokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenAlreadyUsedException.class)
    public ResponseEntity<Map<String, String>> handleInvitationTokenAlreadyUsed(
            InvitationTokenAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleInvitationTokenExpired(
            InvitationTokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFound(CompanyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnallowedRoleException.class)
    public ResponseEntity<Map<String, String>> handleUnallowedRole(UnallowedRoleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<Map<String, String>> handleUnallowedRole(InvalidPinException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadInvitationRequestException.class)
    public ResponseEntity<Map<String, String>> handlePaymentPlanNotFound(BadInvitationRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AttributeAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePaymentPlanNotFound(AttributeAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AttributeCapacityReachedException.class)
    public ResponseEntity<Map<String, String>> handleAttributeCapacity(AttributeCapacityReachedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AttributeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAttributeNotFound(AttributeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PaymentPlanNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePaymentPlanNotFound(PaymentPlanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handlePaymentMethodNotFound(PlanLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PaymentMethodNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePaymentMethodNotFound(PaymentMethodNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.log(Level.SEVERE, "Got generic error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // This forces an HTTP 403 status
                .body(Map.of(
                        "status", 403,
                        "error", "Forbidden",
                        "message", "You do not have permission to access this resource."
                ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}