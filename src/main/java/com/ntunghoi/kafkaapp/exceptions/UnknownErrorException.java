package com.ntunghoi.kafkaapp.exceptions;

public class UnknownErrorException extends Exception {
    public UnknownErrorException(String details, Throwable cause) {
        super(
                String.format("Unknown error: %s", details),
                cause
        );
    }
}
