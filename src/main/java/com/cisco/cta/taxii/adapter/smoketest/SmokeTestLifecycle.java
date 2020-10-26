/*
   Copyright 2016 Cisco Systems

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
package com.cisco.cta.taxii.adapter.smoketest;

import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.http.client.ClientHttpResponse;

import com.cisco.cta.taxii.adapter.AdapterRunner;
import com.cisco.cta.taxii.adapter.RequestFactory;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus.Feed;
import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class SmokeTestLifecycle implements Lifecycle {

    private static final String RESOURCE = "/com/cisco/cta/taxii/adapter/smoketest/taxii-poll-response-smoke-test.xml";

    private final SettingsConfiguration settingsConfig;
    private final RequestFactory requestFactory;
    private final Templates templates;
    private final Writer logWriter;

    @Override
    public void start() {
        logSettingsConfig();
        validateOutput();
        validateTaxiiConnectivity();
        sendTestIncident();
        AdapterRunner.exit();
    }

    void logSettingsConfig() {
        log.info("Listing configuration parameters ...");
        log.info("---------- taxii-service ----------");
        log.info("pollEndpoint={}", settingsConfig.taxiiServiceSettings().getPollEndpoint());
        log.info("username={}", settingsConfig.taxiiServiceSettings().getUsername());
        log.info("password={}", Strings.isNullOrEmpty(settingsConfig.taxiiServiceSettings().getPassword()) ? "" : "*****");
        log.info("feeds");
        for (String feed : settingsConfig.taxiiServiceSettings().getFeeds()) {
            log.info("  {}", feed);
        }
        log.info("---------- schedule ----------");
        log.info("cron={}", settingsConfig.scheduleSettings().getCron());
        log.info("---------- transform ----------");
        log.info("stylesheet={}", settingsConfig.transformSettings().getStylesheet());
        if (settingsConfig.proxySettings() != null) {
            log.info("---------- proxy ----------");
            log.info("url={}", settingsConfig.proxySettings().getUrl());
            log.info("authenticationType={}", settingsConfig.proxySettings().getAuthenticationType());
            log.info("username={}", settingsConfig.proxySettings().getUsername());
            log.info("password={}", Strings.isNullOrEmpty(settingsConfig.proxySettings().getPassword()) ? "" : "*****");
        }
        log.info("---------- end of configuration ----------");
    }

    void validateOutput() {
        log.info("Validating the output configuration in logback.xml ...");
        Logger logger = (Logger) LoggerFactory.getLogger("output");
        if (logger.iteratorForAppenders().hasNext()) {
            log.info("logback.xml: appender-ref is OK");
        } else {
            log.error("Error in logback.xml, no appender-ref is declared inside <logger name=\"output\" ...");
        }
    }

    void validateTaxiiConnectivity() {
        URL endpoint = settingsConfig.taxiiServiceSettings().getPollEndpoint();
        log.info("Contacting TAXII poll service {} ...", endpoint);
        try {
            Feed feed = new TaxiiStatus.Feed();
            feed.setName(settingsConfig.taxiiServiceSettings().getFeeds().iterator().next());
            ClientHttpResponse resp = requestFactory.createPollRequest("smoke-test", feed).execute();

            switch (resp.getRawStatusCode()) {
            case 200:
                log.info("Successfully connected to {}", endpoint);
                return;
            case 401:
            case 403:
                log.error("Authentication or authorization problem, verify your credentials in application.yml");
                return;
            default:
                log.error("{} returned {} HTTP status code, check your configuration", endpoint, resp.getRawStatusCode());
            }

        } catch (UnknownHostException e) {
            log.error("Unable to resolve host name {}, verify your application.yml and your DNS settings", endpoint.toString());
        } catch (Exception e) {
            log.error("Error connecting to " + endpoint, e);
        }
    }

    void sendTestIncident() {
        try (InputStream testResource = SmokeTestLifecycle.class.getResourceAsStream(RESOURCE)) {
            log.info("Sending smoke-test incident to the output configured in logback.xml ...");
            Transformer transformer = templates.newTransformer();
            transformer.transform(new StreamSource(testResource), new StreamResult(logWriter));
            log.info("Please manually validate the result in your target system. Tip: Search your final data destination (SIEM or file) for the word 'smoke-test'.");
        } catch (Exception e) {
            log.error("Error sending test incident", e);
        }
    }


    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean isRunning() {
        return false;
    }

}
