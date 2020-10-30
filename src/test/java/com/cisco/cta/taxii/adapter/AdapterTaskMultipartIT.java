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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static com.cisco.cta.taxii.adapter.PollFulfillmentMatcher.pollFulfillment;
import static com.cisco.cta.taxii.adapter.PollRequestMatcher.initialPollRequest;
import static com.cisco.cta.taxii.adapter.httpclient.HasHeaderMatcher.hasAllTaxiiHeaders;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(
        classes = MockAdapterConfiguration.class,
        initializers = {YamlFileApplicationContextInitializer.class, StatusFileContextInitializer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdapterTaskMultipartIT {

    private static final File OUTPUT_FILE = new File("target/output.json");
    private static final File EXPECTED_OUTPUT_FILE = new File("src/test/resources/expected-output.json");

    @Autowired
    @Qualifier("adapterTask")
    private Runnable task;

    @Autowired
    private ClientHttpRequestFactory httpRequestFactory;

    @Mock
    private ClientHttpRequest httpReq;
    @Mock
    private ClientHttpRequest httpReq2;

    private URI pollServiceUri;
    private HttpHeaders httpReqHeaders;
    private ByteArrayOutputStream httpReqBody;
    private ByteArrayOutputStream httpReq2Body;

    @Mock
    private ClientHttpResponse httpResp;
    @Mock
    private ClientHttpResponse httpResp2;

    private InputStream taxiiPollRespBodyInitial;
    private InputStream taxiiPollRespBodyNext;

    @Autowired
    private AdapterStatistics statistics;

    @Before
    public void setUp() throws Exception {
        initLogbackOutput();
        MockitoAnnotations.initMocks(this);
        pollServiceUri = new URI("https://taxii.cloudsec.sco.cisco.com/skym-taxii-ws/PollService/");
        when(httpRequestFactory.createRequest(pollServiceUri, HttpMethod.POST)).thenReturn(httpReq, httpReq2);
        httpReqHeaders = new HttpHeaders();

        when(httpReq.getHeaders()).thenReturn(httpReqHeaders);
        httpReqBody = new ByteArrayOutputStream();
        when(httpReq.getBody()).thenReturn(httpReqBody);
        when(httpReq.execute()).thenReturn(httpResp);

        when(httpReq2.getHeaders()).thenReturn(httpReqHeaders);
        httpReq2Body = new ByteArrayOutputStream();
        when(httpReq2.getBody()).thenReturn(httpReq2Body);
        when(httpReq2.execute()).thenReturn(httpResp2);

        taxiiPollRespBodyInitial = AdapterTaskMultipartIT.class.getResourceAsStream(
                "/taxii-poll-response-mp-body-initial.xml");
        taxiiPollRespBodyNext = AdapterTaskMultipartIT.class.getResourceAsStream(
                "/taxii-poll-response-mp-body-next.xml");
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
    }

    @Test
    public void runFromEpochBegin() throws Exception {
        initialMultipartRequestResponse();
        assertThat(statistics.getPolls(), is(2L));
        assertThat(statistics.getLogs(), is(2L));
        assertThat(statistics.getErrors(), is(0L));
        assertThat(OUTPUT_FILE + " content expected same as " + EXPECTED_OUTPUT_FILE,
                FileUtils.readFileToString(OUTPUT_FILE), is(FileUtils.readFileToString(EXPECTED_OUTPUT_FILE)));
    }

    private void initialMultipartRequestResponse() throws IOException {
        when(httpResp.getRawStatusCode()).thenReturn(200);
        when(httpResp.getBody()).thenReturn(taxiiPollRespBodyInitial);
        when(httpResp2.getRawStatusCode()).thenReturn(200);
        when(httpResp2.getBody()).thenReturn(taxiiPollRespBodyNext);
        task.run();
        verify(httpRequestFactory, times(2)).createRequest(pollServiceUri, HttpMethod.POST);
        assertThat(httpReqHeaders, hasAllTaxiiHeaders());
        verify(httpReq).execute();
        verify(httpReq2).execute();
        assertThat(httpReqBody, is(initialPollRequest("123", "collection_name")));
        assertThat(httpReq2Body, is(pollFulfillment("123", "collection_name", "1000#2000", "2")));
        httpReqBody.reset();
    }

}
