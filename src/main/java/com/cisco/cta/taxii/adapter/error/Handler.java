package com.cisco.cta.taxii.adapter.error;

public interface Handler<T extends Throwable> {

    void handle(T t) throws Throwable;
}
