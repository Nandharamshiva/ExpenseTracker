package org.example.expensetracker.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {

        ApiError error = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.name())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledAccount(DisabledException ex) {

        ApiError error = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.name())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .orElse("Validation failed");

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(message)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

        @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
        public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
                if (ex == null) {
                        ApiError error = ApiError.builder()
                                        .status(HttpStatus.BAD_REQUEST.value())
                                        .error(HttpStatus.BAD_REQUEST.name())
                                        .message("Invalid request")
                                        .build();

                        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }

                String message = "Invalid request";

                if (ex instanceof MethodArgumentTypeMismatchException m) {
                        String name = m.getName();
                        String value = m.getValue() == null ? "" : String.valueOf(m.getValue());
                        message = "Invalid value for parameter '" + name + "': " + value;
                } else {
                        String exMessage = ex.getMessage();
                        if (exMessage != null && !exMessage.isBlank()) {
                                message = exMessage;
                        }
                }

                ApiError error = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.name())
                                .message(message)
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.name())
                .message("HTTP method " + ex.getMethod() + " is not supported for this endpoint.")
                .build();

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error(HttpStatus.FORBIDDEN.name())
                                .message(ex.getMessage() == null ? "Access denied" : ex.getMessage())
                                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {

                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                ApiError error = ApiError.builder()
                                .status(status.value())
                                .error(status.name())
                                .message(ex.getReason() == null ? status.getReasonPhrase() : ex.getReason())
                                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);

        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .message("Something went wrong")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

