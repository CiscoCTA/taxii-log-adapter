package com.cisco.cta.taxii.adapter.error;

import org.springframework.core.NestedRuntimeException;
import org.springframework.validation.BindException;
import org.yaml.snakeyaml.error.YAMLException;

import com.cisco.cta.taxii.adapter.settings.BindExceptionHandler;


public class ChainHandler implements Handler<Throwable> {

    private final Handler<BindException> bindExceptionHandler = new BindExceptionHandler(System.err);
    private final Handler<YAMLException> yamlExceptionHandler = new YamlExceptionHandler(System.err);
    private final Handler<Throwable> fallBackHandler = new FallBackHandler(System.err);


    @Override
    public void handle(Throwable t) {
        try {
            throw t;

        } catch (NestedRuntimeException e) {
            try {
                throw e.getMostSpecificCause();
            } catch (BindException bindRootCause) {
                bindExceptionHandler.handle(bindRootCause);
            } catch (Throwable unknownRootCause) {
                fallBackHandler.handle(unknownRootCause);
            }
        
        } catch (YAMLException target) {
            yamlExceptionHandler.handle(target);

        } catch (Throwable e) {
            fallBackHandler.handle(e);
        }
    }
    
}
