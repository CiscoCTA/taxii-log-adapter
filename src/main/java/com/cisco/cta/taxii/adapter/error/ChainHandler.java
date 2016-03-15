package com.cisco.cta.taxii.adapter.error;

import org.springframework.core.NestedRuntimeException;
import org.springframework.validation.BindException;

import com.cisco.cta.taxii.adapter.settings.BindExceptionHandler;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChainHandler implements Handler<Throwable> {

    //TODO private final Iterable<Handler> delegates;

    private final Handler<BindException> bindExceptionHandler = new BindExceptionHandler(System.err);

    public void handle(Throwable t) throws Throwable {
        try {
            throw t;

        } catch (NestedRuntimeException e) {
            try {
                throw e.getMostSpecificCause();
            } catch (BindException bindRootCause) {
                bindExceptionHandler.handle(bindRootCause);
            } catch (Throwable unknownRootCause) {
                System.err.println("CRITICAL UNKNOWN ERROR WHILE INITIALIZING");
                throw e;
            }
        
        } catch (Throwable e) {
            System.err.println("CRITICAL UNKNOWN ERROR WHILE INITIALIZING");
            throw e;
        }
    }
    
}
