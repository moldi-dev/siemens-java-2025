package com.siemens.internship.exception;

import com.siemens.internship.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                validationErrors.put(fieldName, errorMessage);
            }

            else {
                String objectName = error.getObjectName();
                String errorMessage = error.getDefaultMessage();
                validationErrors.put(objectName, errorMessage);
            }
        });

        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now().toString())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .errorStatus(HttpStatus.BAD_REQUEST)
                .errorMessage("Validation failed")
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(WebRequest request) {
        ErrorResponse response = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now().toString())
                .errorMessage("The body of the request is missing")
                .errorStatus(HttpStatus.BAD_REQUEST)
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .requestPath(request.getDescription(false))
                .build();

        return new ResponseEntity<>(response, response.getErrorStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        ErrorResponse response = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now().toString())
                .errorMessage(exception.getMessage())
                .errorStatus(HttpStatus.NOT_FOUND)
                .errorCode(HttpStatus.NOT_FOUND.value())
                .requestPath(request.getDescription(false))
                .build();

        return new ResponseEntity<>(response, response.getErrorStatus());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception, WebRequest request) {
        ErrorResponse response = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now().toString())
                .errorMessage(exception.getMessage())
                .errorStatus(HttpStatus.CONFLICT)
                .errorCode(HttpStatus.CONFLICT.value())
                .requestPath(request.getDescription(false))
                .build();

        return new ResponseEntity<>(response, response.getErrorStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        ErrorResponse response = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now().toString())
                .errorMessage(exception.getMessage())
                .errorStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .requestPath(request.getDescription(false))
                .build();

        return new ResponseEntity<>(response, response.getErrorStatus());
    }
}
