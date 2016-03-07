package com.cisco.cta.taxii.adapter.settings;

import static org.junit.Assert.*;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.validation.BindException;

public class BindExceptionHandlerTest {

    private BindExceptionHandler handler;
    
    @Mock
    private PrintStream err;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new BindExceptionHandler(err);
    }

    @Test
    public void invalidPollEndpoint() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(SettingsConfiguration.class);
            PropertySource<?> source = new MockPropertySource()
                    .withProperty("taxiiService.pollEndpoint", "invalid value")
                    .withProperty("schedule.cron", "* * * * * *");
            ctx.getEnvironment().getPropertySources().addFirst(source);
            ctx.refresh();
            fail("The context creation must fail because of invalid configuration.");
        } catch (NestedRuntimeException e) {
            BindException be = (BindException) e.getRootCause();
            handler.handle(be);
            verify(err).println(contains("pollEndpoint has illegal value"));
        }
    }
}
