package com.ntunghoi.kafkaapp.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ntunghoi.kafkaapp.exceptions.BadRequestDataException;
import com.ntunghoi.kafkaapp.exceptions.UnknownErrorException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.pattern.PatternParseException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String DEFAULT_MESSAGE_RESPONSE_BODY_TEMPLATE =
            """
                    {
                         "message": "%s"
                    }
                    """;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing request body for the required data");
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<String> conversionFailedException(ConversionFailedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        var beanClazz = Objects.requireNonNull(exception.getTarget()).getClass();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            try {
                Field field = beanClazz.getDeclaredField(error.getField());
                var jsonPropertyAnnotation = field.getAnnotation(JsonProperty.class);
                String fieldName = jsonPropertyAnnotation != null ? jsonPropertyAnnotation.value() : error.getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            } catch (NoSuchFieldException e) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> signatureException(SignatureException signatureException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(signatureException.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> missingServletRequestParameterException(MissingServletRequestParameterException missingServletRequestParameterException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                String.format("Missing value for parameter %s", missingServletRequestParameterException.getParameterName())
        );
    }

    @ExceptionHandler(UnknownErrorException.class)
    public ResponseEntity<String> unknownErrorException(UnknownErrorException unknownErrorException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        String.format(
                                DEFAULT_MESSAGE_RESPONSE_BODY_TEMPLATE,
                                unknownErrorException.getMessage()
                        )
                );
    }

    @ExceptionHandler(BadRequestDataException.class)
    public ResponseEntity<String> badTransactionsQueryRequestException(BadRequestDataException badTransactionsQueryRequestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        String.format(
                                DEFAULT_MESSAGE_RESPONSE_BODY_TEMPLATE,
                                badTransactionsQueryRequestException.getMessage()
                        )
                );
    }

    @ExceptionHandler(NoSuchBeanDefinitionException.class)
    public ResponseEntity<String> noSuchBeanDefinitionException(NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        String.format(
                                "Missing definition for bean (%s): %s",
                                requireNonNullElse(noSuchBeanDefinitionException.getBeanName(), "unknown bean"),
                                noSuchBeanDefinitionException.getMessage()
                        )
                );
    }

    @ExceptionHandler(PatternParseException.class)
    public ResponseEntity<String> patternParseException(PatternParseException patternParseException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(patternParseException.toDetailedString());
    }
}
