package com.cisco.cta.taxii.adapter.error;

import java.io.PrintStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FallBackHandler implements Handler<Throwable> {

    private final PrintStream err;

    @Override
    public void handle(Throwable t) {
        err.println("CRITICAL UNKNOWN ERROR WHILE INITIALIZING");
        t.printStackTrace(err);
    }

}
