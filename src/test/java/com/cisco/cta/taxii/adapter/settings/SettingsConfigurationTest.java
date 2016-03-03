/*
   Copyright 2015 Cisco Systems

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.cisco.cta.taxii.adapter.settings;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.cisco.cta.taxii.adapter.httpclient.ProxyAuthenticationType;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.net.URL;
import java.util.List;


public class SettingsConfigurationTest {

    @Test
    public void refuseMissingConfiguration() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SettingsConfiguration.class)) {
            fail("The context creation must fail because of missing configuration.");
        } catch (NestedRuntimeException e) {
            BindException be = (BindException) e.getRootCause();
            assertThat(be.getFieldErrors(), is(not(emptyCollectionOf(FieldError.class))));
        }
    }

    @Test
    public void acceptValidConfiguration() throws Exception {
        try (ConfigurableApplicationContext ctx = context(validProperties())) {
            assertThat(ctx.getBean(TaxiiServiceSettings.class).getFeeds().get(0), is("alpha-feed"));
        }
    }

    private ConfigurableApplicationContext context(PropertySource<?> source) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(SettingsConfiguration.class);
        ctx.getEnvironment().getPropertySources().addFirst(source);
        ctx.refresh();
        return ctx;
    }

    private MockPropertySource validProperties() {
        return new MockPropertySource()
            .withProperty("taxiiService.pollEndpoint", "http://taxii")
            .withProperty("taxiiService.username", "smith")
            .withProperty("taxiiService.password", "secret")
            .withProperty("taxiiService.feeds[0]", "alpha-feed")
            .withProperty("taxiiService.statusFile", "taxii-status.xml")
            .withProperty("schedule.cron", "* * * * * *")
            .withProperty("transform.stylesheet", "transform.xsl")
            .withProperty("proxy.url", "http://localhost:8001/")
            .withProperty("proxy.authenticationType", "NONE");
    }

    @Test
    public void typeConversion() throws Exception {
        try (ConfigurableApplicationContext ctx = context(validProperties())) {
            assertThat(ctx.getBean(ProxySettings.class).getUrl(), is(new URL("http://localhost:8001/")));
            assertThat(ctx.getBean(ProxySettings.class).getAuthenticationType(), is(ProxyAuthenticationType.NONE));
        }
    }

    @Test
    public void resfuseMissingPollEndpoint() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxiiService.pollEndpoint"))) {
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

    private MockPropertySource exclude(MockPropertySource all, String excludePrefix) {
        MockPropertySource source = new MockPropertySource();
        for (String key : all.getPropertyNames()) {
            if (! key.startsWith(excludePrefix)) {
                source.setProperty(key, all.getProperty(key));
            }
        }
        return source;
    }

    @Test
    public void loadFeedNamesFromFile() throws Exception {
        try (ConfigurableApplicationContext ctx = context(
                exclude(validProperties(), "taxiiService.feeds")
                .withProperty("taxiiService.feedNamesFile", "src/test/resources/feed-names.txt"))) {
            List<String> feeds = ctx.getBean(TaxiiServiceSettings.class).getFeeds();
            assertThat(feeds, contains("little-feed", "big-feed"));
        }
    }


    @Test
    public void refuseMissingFeedNames() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxiiService.feeds"))) {
            fail("The context creation must fail because of invalid configuration.");
        } catch (BeanCreationException e) {
            assertThat(e.getRootCause(), instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void statusFileDefault() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxiiService.statusFile"))) {
            TaxiiServiceSettings settings = ctx.getBean(TaxiiServiceSettings.class);
            assertThat(settings.getStatusFile().getPath(), is("taxii-status.xml"));
        }
    }

}
