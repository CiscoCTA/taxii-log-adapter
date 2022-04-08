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
package com.cisco.cta.taxii.adapter.smoketest;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.cisco.cta.taxii.adapter.smoketest.ContainsMessageMatcher.containsMessage;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ContextConfiguration(classes = {SmokeTestConfiguration.class})
@ActiveProfiles("smoketest")
@TestPropertySource(properties={
    "taxii-service.pollEndpoint=http://localhost:8098/taxii/service",
    "taxii-service.username=user",
    "taxii-service.password=secret",
    "taxii-service.feeds[0]=secured-feed",
    "taxii-service.statusFile=target/auth-fail-status.xml",
    "transform.stylesheet=src/test/resources/config/stix2stix.xsl",
    "schedule.cron=0 0 * * * *"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DetectTaxiiAuthenticationFailureTest {

    @Autowired
    private SmokeTestLifecycle smokeTestLifecycle;

    private Logger logger;
    private Appender<ILoggingEvent> appender;
    private Server server;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        appender = mock(Appender.class);
        logger = (Logger) LoggerFactory.getLogger(SmokeTestLifecycle.class);
        logger.addAppender(appender);
        server = new Server(8098);
        server.setHandler(new UnauthorizedHandler());
        server.start();
    }

    private static class UnauthorizedHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.sendError(403, "Unauthorized");
        }
        
    }

    @After
    public void tearDown() throws Exception {
        logger.detachAppender(appender);
        server.stop();
        server.join();
    }


    @Test
    public void failOnTaxiiAuthentication() throws Exception {
        smokeTestLifecycle.validateTaxiiConnectivity();
        verify(appender).doAppend(argThat(containsMessage("verify your credentials in application.yml")));
    }
}
