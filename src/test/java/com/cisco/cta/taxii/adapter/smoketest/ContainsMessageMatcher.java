package com.cisco.cta.taxii.adapter.smoketest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ContainsMessageMatcher extends TypeSafeMatcher<ILoggingEvent> {

    public static Matcher<ILoggingEvent> containsMessage(String message) {
        return new ContainsMessageMatcher(message);
    }


    private final String message;

    @Override
    public void describeTo(Description description) {
        description.appendText("log message containing ");
        description.appendValue(message);
    }

    @Override
    protected boolean matchesSafely(ILoggingEvent event) {
        return event.getFormattedMessage().contains(message);
    }

}