package com.cisco.cta.taxii.adapter;

public class JsonValidationException extends RuntimeException {

    public JsonValidationException(String message) {
        super("Json validation failed: " + message);
    }

}
