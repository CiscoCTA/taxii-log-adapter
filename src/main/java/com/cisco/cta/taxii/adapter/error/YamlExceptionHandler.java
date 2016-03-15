package com.cisco.cta.taxii.adapter.error;

import java.io.PrintStream;

import org.yaml.snakeyaml.error.YAMLException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YamlExceptionHandler implements Handler<YAMLException> {

    private final PrintStream err;

    @Override
    public void handle(YAMLException e) throws Throwable {
        err.println("Error parsing application.yml: " + e.getMessage());
    }

}
