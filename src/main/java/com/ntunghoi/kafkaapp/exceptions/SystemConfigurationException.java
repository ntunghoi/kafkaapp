package com.ntunghoi.kafkaapp.exceptions;

public class SystemConfigurationException extends Exception {
    public SystemConfigurationException(String message) {
        super(String.format("Problem found in system configuration: %s", message));
    }
}
