package com.cisco.cta.taxii.adapter.smoketest;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cisco.cta.taxii.adapter.AdapterConfiguration;
import static com.cisco.cta.taxii.adapter.smoketest.ContainsMessageMatcher.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


@ContextConfiguration(classes = {SmokeTestConfiguration.class, AdapterConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("smoketest")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SmokeTestLifecycleTest {

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
    public void logConfig() throws Exception {
        smokeTestLifecycle.logSettingsConfig();
        verify(appender).doAppend(argThat(containsMessage("pollEndpoint=https://taxii.cloudsec.sco.cisco.com/skym-taxii-ws/PollService")));
        verify(appender).doAppend(argThat(containsMessage("username=user")));
        verify(appender, times(2)).doAppend(argThat(containsMessage("password=*****"))); // TAXII & PROXY
        verify(appender).doAppend(argThat(containsMessage("feeds")));
        verify(appender).doAppend(argThat(containsMessage("collection_name")));
        verify(appender).doAppend(argThat(containsMessage("cron=0 0 * * * *")));
        verify(appender).doAppend(argThat(containsMessage("stylesheet=config/stix2json.xsl")));
        verify(appender).doAppend(argThat(containsMessage("url=http://localhost:8002")));
        verify(appender).doAppend(argThat(containsMessage("authenticationType=BASIC")));
        verify(appender).doAppend(argThat(containsMessage("username=proxyuser")));
      }
}
