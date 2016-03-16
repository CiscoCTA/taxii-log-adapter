package com.cisco.cta.taxii.adapter.error;

import java.io.PrintStream;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CauseHandler implements Handler<Throwable> {

    private final PrintStream err;


    @Override
    public void handle(Throwable t) {
        if (t.getMessage() != null) {
            err.println(t);
        }
        if (t.getCause() != null) {
            handle(t.getCause());
        }
    }

}
