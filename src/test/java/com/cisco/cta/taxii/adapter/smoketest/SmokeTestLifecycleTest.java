package com.cisco.cta.taxii.adapter.smoketest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
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

import static com.cisco.cta.taxii.adapter.smoketest.ContainsMessageMatcher.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;


@ContextConfiguration(classes = {SmokeTestConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("smoketest")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SmokeTestLifecycleTest {

    private static final File OUTPUT_FILE = new File("target/output.json");
    private static final File EXPECTED_OUTPUT_FILE = new File("src/test/resources/expected-smoke-test-output.json");

    @Autowired
    private SmokeTestLifecycle smokeTestLifecycle;

    private Logger logger;
    private Appender<ILoggingEvent> appender;
    LoggerContext loggerContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        appender = mock(Appender.class);
        logger = (Logger) LoggerFactory.getLogger(SmokeTestLifecycle.class);
        logger.addAppender(appender);
        loggerContext = logger.getLoggerContext();
    }

    @After
    public void tearDown() throws Exception {
        logger.detachAppender(appender);
        loggerContext.stop();
        ContextInitializer logInitializer = new ContextInitializer(loggerContext);
        logInitializer.autoConfig();
        loggerContext.start();
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

    @Test
    public void successOnValidOutputAppenders() throws Exception {
        smokeTestLifecycle.validateOutput();
        verify(appender).doAppend(argThat(containsMessage("logback.xml: appender-ref is OK")));
    }

    @Test
    public void failOnMissingOutputAppenders() throws Exception {
        loadLogConfig("/logback-missing-appenders.xml");
        smokeTestLifecycle.validateOutput();
        verify(appender).doAppend(argThat(containsMessage("Error in logback.xml, no appender-ref is declared")));
    }

    private void loadLogConfig(String resource) throws JoranException, Exception, IOException {
        try (InputStream in = SmokeTestLifecycleTest.class.getResourceAsStream(resource)) {
            loggerContext.stop();
            JoranConfigurator logConfigurator = new JoranConfigurator();
            logConfigurator.setContext(loggerContext);
            logConfigurator.doConfigure(in);
            loggerContext.start();
            setUp(); //re-initialize logger
        }
    }

    @Test
    public void writeTestIncident() throws Exception {
        smokeTestLifecycle.sendTestIncident();
        assertThat(OUTPUT_FILE + " content expected same as " + EXPECTED_OUTPUT_FILE,
                FileUtils.readFileToString(OUTPUT_FILE), is(FileUtils.readFileToString(EXPECTED_OUTPUT_FILE)));
        verify(appender).doAppend(argThat(containsMessage("Please manually validate the result in your target system.")));
    }
}
