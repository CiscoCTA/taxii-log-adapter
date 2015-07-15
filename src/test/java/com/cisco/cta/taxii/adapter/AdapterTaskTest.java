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

import static com.cisco.cta.taxii.adapter.IsEventContaining.verifyLog;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import ch.qos.logback.core.Appender;

import com.cisco.cta.taxii.adapter.AdapterStatistics;
import com.cisco.cta.taxii.adapter.AdapterTask;
import com.cisco.cta.taxii.adapter.RequestFactory;
import com.cisco.cta.taxii.adapter.ResponseHandler;
import com.google.common.collect.ImmutableList;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


@SuppressWarnings({"rawtypes", "unchecked"})
public class AdapterTaskTest {

    private Runnable task;
    
    @Mock
    private RequestFactory requestFactory;

    @Mock
    private ClientHttpRequest request;
    
    @Mock
    private ClientHttpResponse response;
    
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private Appender mockAppender;

    @Mock
    private TaxiiServiceSettings settings;

    @Spy
    private AdapterStatistics statistics = new AdapterStatistics();

    @Mock
    private TaxiiStatusDao taxiiStatusDao;

    private DatatypeFactory datatypeFactory;

    private Clock clock;

    private Instant now;

   @Before
    public void setUp() throws Exception {
       datatypeFactory = DatatypeFactory.newInstance();
       now = Instant.parse("2000-01-02T03:04:05.006Z");
       clock = Clock.fixed(now, ZoneId.systemDefault());
       MockitoAnnotations.initMocks(this);
       ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AdapterTask.class)).addAppender(mockAppender);
       when(settings.getFeeds()).thenReturn(ImmutableList.of("my-collection"));
       task = new AdapterTask(requestFactory, responseHandler, settings, statistics, taxiiStatusDao, datatypeFactory, clock);
       when(requestFactory.createInitialRequest(anyString(), anyString())).thenReturn(request);
    }

    @Test
    public void triggerRequestResponse() throws Exception {
        when(request.execute()).thenReturn(response);
        task.run();
        verify(requestFactory).createInitialRequest(anyString(), anyString());
        verify(request).execute();
        verify(responseHandler).handle("my-collection", response);
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(0L));
    }

    @Test
    public void handleCommunicationError() throws Exception {
        when(request.execute()).thenThrow(IOException.class);
        task.run();
        verify(requestFactory).createInitialRequest(anyString(), anyString());
        verify(request).execute();
        verifyZeroInteractions(responseHandler);
        verifyLog(mockAppender, "Error");
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(1L));
    }

    @Test
    public void handleInvalidResponseError() throws Exception {
        when(request.execute()).thenReturn(response);
        doThrow(new Exception("Dummy response")).when(responseHandler).handle("my-collection", response);
        task.run();
        verify(requestFactory).createInitialRequest(anyString(), anyString());
                verify(request).execute();
        verifyLog(mockAppender, "Error");
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(1L));
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsNotProvided() throws Exception {
        when(responseHandler.handle(anyString(), any(ClientHttpResponse.class))).thenReturn(TaxiiPollResponse.builder().inclusiveEndTime(null).build());
        task.run();
        verify(taxiiStatusDao).update("my-collection", datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00"));
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsProvided() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        when(responseHandler.handle(anyString(), any(ClientHttpResponse.class))).thenReturn(TaxiiPollResponse.builder().inclusiveEndTime(cal).build());
        task.run();
        verify(taxiiStatusDao).update("my-collection", cal);
    }

    @Test
    public void doNotWriteLastUpdateIfMultipartFails() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse firstResponse = TaxiiPollResponse.builder().more(true).resultPartNumber(1).build();
        when(responseHandler.handle(anyString(), any(ClientHttpResponse.class))).thenReturn(firstResponse, null);
        task.run();
        verify(taxiiStatusDao, times(0)).update("my-collection", cal);
    }

    @Test
    public void writeLastUpdateAfterMultipartFetched() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse firstResponse = TaxiiPollResponse.builder().more(true).resultPartNumber(1).build();
        TaxiiPollResponse secondResponse = TaxiiPollResponse.builder().more(false).resultPartNumber(2).build();
        when(responseHandler.handle(anyString(), any(ClientHttpResponse.class))).thenReturn(firstResponse, secondResponse);
        when(requestFactory.createFulfillmentRequest(anyString(), anyString(), anyString(), anyInt())).thenReturn(request);
        task.run();
        verify(taxiiStatusDao).update("my-collection", cal);
    }

}
