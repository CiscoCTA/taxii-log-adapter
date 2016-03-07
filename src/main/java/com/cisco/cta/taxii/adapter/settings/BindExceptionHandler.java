package com.cisco.cta.taxii.adapter.settings;

import java.io.PrintStream;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BindExceptionHandler {

    private final PrintStream err;

    public void handle(BindException e) {
        for(ObjectError error : e.getAllErrors()) {
            StringBuilder b = new StringBuilder()
                .append("Error in application.yml")
                .append(", section ").append(error.getObjectName());
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                b.append(", ").append(fieldError.getField())
                .append(" has illegal value ").append(fieldError.getRejectedValue());
            }
            err.println(b.toString());
        }
    }
}
