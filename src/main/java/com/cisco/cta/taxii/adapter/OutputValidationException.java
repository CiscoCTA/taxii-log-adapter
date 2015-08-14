package com.cisco.cta.taxii.adapter;

public class OutputValidationException extends RuntimeException {
    public static final String MDC_KEY = "OutputValidationException";

    public OutputValidationException(String message) {
        super("Output validation failed: " + message);
    }

}
