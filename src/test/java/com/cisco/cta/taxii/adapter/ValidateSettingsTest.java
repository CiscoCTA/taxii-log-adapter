package com.cisco.cta.taxii.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.net.URL;


public class ValidateSettingsTest {

    @Test
    public void refuseMissingConfiguration() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SettingsTestConfiguration.class)) {
            fail("The context creation must fail because of missing configuration.");
        } catch (NestedRuntimeException e) {
            BindException be = (BindException) e.getRootCause();
            assertThat(be.getFieldErrors(), is(not(emptyCollectionOf(FieldError.class))));
        }
    }

    @Test
    public void acceptValidConfiguration() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(SettingsTestConfiguration.class);
            PropertySource<?> source = new MockPropertySource()
                .withProperty("taxiiService.pollEndpoint", "http://taxii")
                .withProperty("taxiiService.username", "smith")
                .withProperty("taxiiService.password", "secret")
                .withProperty("taxiiService.feeds[0]", "alpha-feed")
                .withProperty("taxiiService.statusFile", "taxii-status.xml")
                .withProperty("schedule.cron", "* * * * * *")
                .withProperty("transform.stylesheet", "transform.xsl")
                .withProperty("proxy.url", "http://localhost:8001/")
                .withProperty("proxy.authenticationType", ProxyAuthenticationType.NONE.name());
            ctx.getEnvironment().getPropertySources().addFirst(source);
            ctx.refresh();
        }
    }

    @Test
    public void typeConversion() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(SettingsTestConfiguration.class);
            PropertySource<?> source = new MockPropertySource()
                    .withProperty("taxiiService.pollEndpoint", "http://taxii")
                    .withProperty("taxiiService.username", "smith")
                    .withProperty("taxiiService.password", "secret")
                    .withProperty("taxiiService.feeds[0]", "alpha-feed")
                    .withProperty("taxiiService.statusFile", "taxii-status.xml")
                    .withProperty("schedule.cron", "* * * * * *")
                    .withProperty("transform.stylesheet", "transform.xsl")
                    .withProperty("proxy.url", "http://localhost:8001/")
                    .withProperty("proxy.authenticationType", ProxyAuthenticationType.NONE.name());
            ctx.getEnvironment().getPropertySources().addFirst(source);
            ctx.refresh();
            assertThat(ctx.getBean(ProxySettings.class).getUrl(), is(new URL("http://localhost:8001/")));
            assertThat(ctx.getBean(ProxySettings.class).getAuthenticationType(), is(ProxyAuthenticationType.NONE));
        }
    }

    @Test
    public void resfuseMissingPollEndpoint() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(SettingsTestConfiguration.class);
            PropertySource<?> source = new MockPropertySource()
                .withProperty("taxiiService.username", "smith")
                .withProperty("taxiiService.password", "secret")
                .withProperty("taxiiService.feeds[0]", "alpha-feed")
                .withProperty("taxiiService.statusFile", "taxii-status.xml")
                .withProperty("schedule.cron", "* * * * * *")
                .withProperty("transform.stylesheet", "transform.xsl");
            ctx.getEnvironment().getPropertySources().addFirst(source);
            ctx.refresh();
            fail("The context creation must fail because of invalid configuration.");
        } catch (NestedRuntimeException e) {
            BindException be = (BindException) e.getRootCause();
            assertThat(be.getFieldErrors(), hasSize(1));
            assertThat(be.getFieldError().getObjectName(), is("taxiiService"));
            assertThat(be.getFieldError().getField(), is("pollEndpoint"));
            assertThat(be.getFieldError().getRejectedValue(), is(nullValue()));
            assertThat(be.getFieldError().getDefaultMessage(), is("may not be null"));
        }
    }

}
