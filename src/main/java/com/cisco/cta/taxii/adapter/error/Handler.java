package com.cisco.cta.taxii.adapter.error;

public interface Handler {

    void handle(Throwable t) throws Throwable;
}
