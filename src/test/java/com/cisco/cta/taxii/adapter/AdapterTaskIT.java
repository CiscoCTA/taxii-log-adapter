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

package com.cisco.cta.taxii.adapter;

import static com.cisco.cta.taxii.adapter.HasHeaderMatcher.hasAllTaxiiHeaders;
import static com.cisco.cta.taxii.adapter.PollResponseMatcher.initialPollRequest;
import static com.cisco.cta.taxii.adapter.PollResponseMatcher.nextPollRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cisco.cta.taxii.adapter.AdapterStatistics;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


@ContextConfiguration(
    classes = MockAdapterConfiguration.class,
    initializers = {ConfigFileApplicationContextInitializer.class, StatusFileContextInitializer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdapterTaskIT {

    private static final File OUTPUT_FILE = new File("target/output.json");
    private static final File EXPECTED_OUTPUT_FILE = new File("src/test/resources/expected-output.json");

    @Autowired
    private Runnable task;
    
    @Autowired
    private ClientHttpRequestFactory httpRequestFactory;

    @Mock
    private ClientHttpRequest httpReq;

    private URI pollServiceUri;
    private HttpHeaders httpReqHeaders;
    private ByteArrayOutputStream httpReqBody;

    @Mock
    private ClientHttpResponse httpResp;

    @Autowired
    private Unmarshaller taxiiRequestMarshaller;
    
    private InputStream taxiiPollRespBodyInitial;
    private InputStream taxiiPollRespBodyNext;
    private InputStream taxiiStatusMsgBody;

    @Autowired
    private DatatypeFactory datatypeFactory;

    @Autowired
    private AdapterStatistics statistics;

    @Before
    public void setUp() throws Exception {
        initLogbackOutput();
        MockitoAnnotations.initMocks(this);
        pollServiceUri = new URI("http://localhost:8080/skym-taxii-ws/PollService/");
        when(httpRequestFactory.createRequest(pollServiceUri, HttpMethod.POST)).thenReturn(httpReq);
        httpReqHeaders = new HttpHeaders();
        when(httpReq.getHeaders()).thenReturn(httpReqHeaders);
        httpReqBody = new ByteArrayOutputStream();
        when(httpReq.getBody()).thenReturn(httpReqBody);
        when(httpReq.execute()).thenReturn(httpResp);
        taxiiPollRespBodyInitial = AdapterTaskIT.class.getResourceAsStream(
                "/taxii-poll-response-body-initial.xml");
        taxiiPollRespBodyNext = AdapterTaskIT.class.getResourceAsStream(
                "/taxii-poll-response-body-next.xml");
        taxiiStatusMsgBody = AdapterTaskIT.class.getResourceAsStream(
                "/taxii-status-message-body.xml");
    }

    private void initLogbackOutput() throws JoranException {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure("src/test/resources/logback-test.xml");
    }

    @After
    public void tearDown() throws Exception {
        taxiiPollRespBodyInitial.close();
        taxiiPollRespBodyNext.close();
        taxiiStatusMsgBody.close();
    }

    @Test
    public void runFromEpochBegin() throws Exception {
        initialRequestResponse();
        nextRequestResponse(2);
        assertThat(statistics.getPolls(), is(2L));
        assertThat(statistics.getLogs(), is(2L));
        assertThat(statistics.getErrors(), is(0L));
        assertTrue(
                OUTPUT_FILE + " content expected same as " + EXPECTED_OUTPUT_FILE,
                FileUtils.contentEquals(OUTPUT_FILE, EXPECTED_OUTPUT_FILE));
    }

    @Test
    public void handleStatusMessage() throws Exception {
        initialRequestResponse();
        requestStatusMessage(2);
        nextRequestResponse(3);
        assertThat(statistics.getPolls(), is(3L));
        assertThat(statistics.getLogs(), is(2L));
        assertThat(statistics.getErrors(), is(0L));
        assertTrue(
                OUTPUT_FILE + " content expected same as " + EXPECTED_OUTPUT_FILE,
                FileUtils.contentEquals(OUTPUT_FILE, EXPECTED_OUTPUT_FILE));
    }

    private void initialRequestResponse() throws IOException {
        when(httpResp.getRawStatusCode()).thenReturn(200);
        when(httpResp.getBody()).thenReturn(taxiiPollRespBodyInitial);
        task.run();
        verify(httpRequestFactory).createRequest(pollServiceUri, HttpMethod.POST);
        verify(httpReq).execute();
        assertThat(httpReqHeaders, hasAllTaxiiHeaders());
        assertThat(httpReqBody, is(initialPollRequest("collection_name")));
        httpReqBody.reset();
    }

    private void nextRequestResponse(int count) throws IOException {
        when(httpResp.getRawStatusCode()).thenReturn(200);
        when(httpResp.getBody()).thenReturn(taxiiPollRespBodyNext);
        task.run();
        verify(httpRequestFactory, times(count)).createRequest(pollServiceUri, HttpMethod.POST);
        verify(httpReq, times(count)).execute();
        assertThat(httpReqHeaders, hasAllTaxiiHeaders());
        assertThat(httpReqBody, is(nextPollRequest(
                "collection_name",
                "2000-12-24T01:02:03.004+01:00")));
        httpReqBody.reset();
    }

    private void requestStatusMessage(int count) throws IOException {
        when(httpResp.getRawStatusCode()).thenReturn(200);
        when(httpResp.getBody()).thenReturn(taxiiStatusMsgBody);
        task.run();
        verify(httpRequestFactory, times(count)).createRequest(pollServiceUri, HttpMethod.POST);
        verify(httpReq, times(count)).execute();
        assertThat(httpReqHeaders, hasAllTaxiiHeaders());
        assertThat(httpReqBody, is(nextPollRequest(
                "collection_name",
                "2000-12-24T01:02:03.004+01:00")));
        httpReqBody.reset();
    }

}
