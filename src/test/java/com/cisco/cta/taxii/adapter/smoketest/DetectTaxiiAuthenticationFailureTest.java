package com.cisco.cta.taxii.adapter.smoketest;

import static com.cisco.cta.taxii.adapter.smoketest.ContainsMessageMatcher.containsMessage;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


@ContextConfiguration(classes = {SmokeTestConfiguration.class})
@ActiveProfiles("smoketest")
@TestPropertySource(properties={
    "taxiiService.pollEndpoint=http://localhost:8098/taxii/service",
    "taxiiService.username=user",
    "taxiiService.password=secret",
    "taxiiService.feeds[0]=secured-feed",
    "taxiiService.statusFile=target/auth-fail-status.xml",
    "transform.stylesheet=config/stix2stix.xsl",
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
