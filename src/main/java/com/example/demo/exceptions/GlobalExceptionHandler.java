package com.example.demo.exceptions;

import com.example.demo.repositories.PaymentHistoryRepository;
import com.example.demo.services.PaymentHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Single place where every exception thrown by a controller/service/repository
 * gets translated into an HTTP status + a consistent JSON body.
 *
 * Without this, anything not explicitly caught inside a controller bubbles up
 * as Spring's default error page: a raw 500 with the exception's stack trace
 * serialized back to the client. That's a security leak (exposes internals,
 * SQL, package structure) and useless for API consumers who need a status
 * code and message they can branch on.
 *
 * Ordering note: Spring picks the most specific matching @ExceptionHandler,
 * so having both a narrow handler (e.g. InsufficientFundsException) and a
 * broad one (Exception) is safe - the narrow one wins when both would match.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Business-rule failure: the source account doesn't have enough balance.
     * Client's fault in the sense that retrying with the same input will
     * never succeed - 422 signals "well-formed request, can't be processed".
     */
    @ExceptionHandler(PaymentHistoryRepository.InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            PaymentHistoryRepository.InsufficientFundsException ex, WebRequest request) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    /**
     * A request with the same Idempotency-Key is already being processed.
     * Previously handled inline in PaymentHistoryController; centralized here
     * so every endpoint that might raise it behaves the same way.
     */
    @ExceptionHandler(PaymentHistoryService.DuplicateRequestInProgressException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRequest(
            PaymentHistoryService.DuplicateRequestInProgressException ex, WebRequest request) {
        log.warn("Duplicate in-flight request: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Bad input: missing/invalid account numbers, non-positive amounts, etc.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Unexpected but non-corrupt state (e.g. idempotency reservation vanished
     * between insert and read, or a balance update matched zero rows).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * LoanRepository/UserDetailsRepository do `queryForList(...).get(0)` to
     * fetch a single row by account number; if nothing matches, that throws
     * IndexOutOfBoundsException rather than returning empty/null. Mapping it
     * to 404 here turns "no such account" into a sane response instead of a
     * confusing 500.
     */
    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            IndexOutOfBoundsException ex, WebRequest request) {
        log.warn("Requested resource not found");
        return build(HttpStatus.NOT_FOUND, "The requested resource could not be found", request);
    }

    /**
     * Malformed date strings (e.g. loan startDate not in yyyy-MM-dd).
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(
            DateTimeParseException ex, WebRequest request) {
        log.warn("Invalid date format: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Invalid date format, expected yyyy-MM-dd", request);
    }

    /**
     * Anything from the JDBC layer that @Retryable didn't recover from
     * (connection failures, constraint violations, etc). Message is
     * intentionally generic - the real DB/driver detail goes to the log,
     * not the client.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(
            DataAccessException ex, WebRequest request) {
        log.error("Database error", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "A database error occurred while processing your request. Please try again.", request);
    }

    /**
     * Catch-all safety net. Full exception is logged server-side; client
     * only ever sees a generic message, never a stack trace or class name.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("^uri=", "");
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(body);
    }
}
