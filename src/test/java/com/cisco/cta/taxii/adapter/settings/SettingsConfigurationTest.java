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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

import com.cisco.cta.taxii.adapter.httpclient.ProxyAuthenticationType;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;

import java.net.URL;
import java.util.List;


public class SettingsConfigurationTest {

    @Test
    public void refuseMissingConfiguration() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SettingsConfiguration.class)) {
            fail("The context creation must fail because of missing configuration.");
        } catch (NestedRuntimeException e) {
            BeanCreationException be = (BeanCreationException) e;
            assertThat(be.getMessage(), containsString("taxiiServiceSettings"));
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
            .withProperty("taxii-service.pollEndpoint", "http://taxii")
            .withProperty("taxii-service.username", "smith")
            .withProperty("taxii-service.password", "secret")
            .withProperty("taxii-service.feeds[0]", "alpha-feed")
            .withProperty("taxii-service.statusFile", "taxii-status.xml")
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
    public void refuseMissingPollEndpoint() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxii-service.pollEndpoint"))) {
            fail("The context creation must fail because of invalid configuration.");
        } catch (NestedRuntimeException e) {
            BindValidationException be = (BindValidationException) e.getRootCause();
            assertThat(be.getValidationErrors().getAllErrors(), hasSize(1));
            assertThat(be.getValidationErrors().getAllErrors().get(0).getCodes()[0], is("NotNull.taxii-service.pollEndpoint"));
            assertThat(be.getValidationErrors().getAllErrors().get(0).getDefaultMessage(), is("must not be null"));
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
                exclude(validProperties(), "taxii-service.feeds")
                .withProperty("taxii-service.feedNamesFile", "src/test/resources/feed-names.txt"))) {
            List<String> feeds = ctx.getBean(TaxiiServiceSettings.class).getFeeds();
            assertThat(feeds, contains("little-feed", "big-feed"));
        }
    }


    @Test
    public void refuseMissingFeedNames() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxii-service.feeds"))) {
            fail("The context creation must fail because of invalid configuration.");
        } catch (BeanCreationException e) {
            assertThat(e.getRootCause(), instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void statusFileDefault() throws Exception {
        try (ConfigurableApplicationContext ctx = context(exclude(validProperties(), "taxii-service.statusFile"))) {
            TaxiiServiceSettings settings = ctx.getBean(TaxiiServiceSettings.class);
            assertThat(settings.getStatusFile().getPath(), is("taxii-status.xml"));
        }
    }

}
