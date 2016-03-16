package com.cisco.cta.taxii.adapter.smoketest;

import static com.cisco.cta.taxii.adapter.smoketest.ContainsMessageMatcher.containsMessage;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


@ContextConfiguration(classes = {SmokeTestConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("smoketest")
@TestPropertySource(properties={
    "taxiiService.pollEndpoint=http://nosuchsite",
    "proxy.url=http://nosuchsite"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DetectUnreachableTaxiiTest {

    @Autowired
    private SmokeTestLifecycle smokeTestLifecycle;

    private Logger logger;
    private Appender<ILoggingEvent> appender;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        appender = mock(Appender.class);
        logger = (Logger) LoggerFactory.getLogger(SmokeTestLifecycle.class);
        logger.addAppender(appender);
    }

    @After
    public void tearDown() throws Exception {
        logger.detachAppender(appender);
    }


    @Test
    public void failOnUnreachableTaxiiEndpoint() throws Exception {
        smokeTestLifecycle.validateTaxiiConnectivity();
        verify(appender).doAppend(argThat(containsMessage("Unable to resolve host name nosuchsite, verify your application.yml and your DNS settings")));
    }
}