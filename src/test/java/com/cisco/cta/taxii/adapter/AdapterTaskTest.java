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

import ch.qos.logback.core.Appender;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import com.google.common.collect.ImmutableList;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;

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
    private ResponseTransformer responseTransformer;

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

    private TaxiiStatus.Feed feed;


   @Before
    public void setUp() throws Exception {
       datatypeFactory = DatatypeFactory.newInstance();
       Instant now = Instant.parse("2000-01-02T03:04:05.006Z");
       clock = Clock.fixed(now, ZoneId.systemDefault());
       MockitoAnnotations.initMocks(this);
       ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AdapterTask.class)).addAppender(mockAppender);
       when(settings.getFeeds()).thenReturn(ImmutableList.of("my-collection"));
       task = new AdapterTask(requestFactory, responseTransformer, settings, statistics, taxiiStatusDao);
       when(requestFactory.createPollRequest(anyString(), any(TaxiiStatus.Feed.class))).thenReturn(request);
       feed = new TaxiiStatus.Feed();
       feed.setName("my-collection");
    }

    @Test
    public void triggerRequestResponse() throws Exception {
        when(request.execute()).thenReturn(response);
        when(taxiiStatusDao.find(anyString())).thenReturn(feed);
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse pollResponse = TaxiiPollResponse.builder().multipart(false).inclusiveEndTime(cal).build();
        when(responseTransformer.transform(any(ClientHttpResponse.class))).thenReturn(pollResponse);
        task.run();
        verify(requestFactory).createPollRequest(anyString(), any(TaxiiStatus.Feed.class));
        verify(request).execute();
        verify(responseTransformer).transform(response);
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(0L));
    }

    @Test
    public void triggerMultipartRequestResponse() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse firstResponse = TaxiiPollResponse.builder().more(true).resultPartNumber(1).inclusiveEndTime(cal).build();
        XMLGregorianCalendar cal2 = datatypeFactory.newXMLGregorianCalendar("2000-01-10T03:04:06.006+00:00");
        TaxiiPollResponse secondResponse = TaxiiPollResponse.builder().more(false).resultPartNumber(2).inclusiveEndTime(cal2).build();
        when(responseTransformer.transform(any(ClientHttpResponse.class))).thenReturn(firstResponse, secondResponse);
        when(requestFactory.createFulfillmentRequest(anyString(), any(TaxiiStatus.Feed.class), anyString(), anyInt())).thenReturn(request);
        task.run();
        verify(responseTransformer, times(2)).transform(any(ClientHttpResponse.class));
        verify(requestFactory).createFulfillmentRequest(anyString(), any(TaxiiStatus.Feed.class), anyString(), anyInt());
    }


    @Test
    public void doNotWriteLastUpdateIfMultipartFails() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse firstResponse = TaxiiPollResponse.builder().more(true).resultPartNumber(1).inclusiveEndTime(cal).build();
        when(requestFactory.createFulfillmentRequest(anyString(), any(TaxiiStatus.Feed.class), anyString(), anyInt())).thenReturn(request);
        when(responseTransformer.transform(any(ClientHttpResponse.class))).thenReturn(firstResponse).thenThrow(new Exception());
        task.run();
        TaxiiStatus.Feed expectedFeed = new TaxiiStatus.Feed();
        expectedFeed.setName("my-collection");
        expectedFeed.setLastUpdate(cal);
        verify(taxiiStatusDao).updateOrAdd(expectedFeed);
    }

    @Test
    public void writeLastUpdateAfterMultipartFetched() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        TaxiiPollResponse firstResponse = TaxiiPollResponse.builder().multipart(true).more(true).resultId("123#456").resultPartNumber(1).inclusiveEndTime(cal).build();
        XMLGregorianCalendar cal2 = datatypeFactory.newXMLGregorianCalendar("2000-01-10T03:04:06.006+00:00");
        TaxiiPollResponse secondResponse = TaxiiPollResponse.builder().more(false).resultPartNumber(2).inclusiveEndTime(cal2).build();
        when(requestFactory.createFulfillmentRequest(anyString(), any(TaxiiStatus.Feed.class), anyString(), anyInt())).thenReturn(request);
        when(responseTransformer.transform(any(ClientHttpResponse.class))).thenReturn(firstResponse, secondResponse);
        task.run();
        InOrder inOrder = Mockito.inOrder(taxiiStatusDao);
        TaxiiStatus.Feed expectedFeed = new TaxiiStatus.Feed();
        expectedFeed.setName("my-collection");
        expectedFeed.setMore(true);
        expectedFeed.setResultId("123#456");
        expectedFeed.setResultPartNumber(1);
        expectedFeed.setLastUpdate(cal);
        inOrder.verify(taxiiStatusDao).updateOrAdd(expectedFeed);
        expectedFeed = new TaxiiStatus.Feed();
        expectedFeed.setName("my-collection");
        expectedFeed.setLastUpdate(cal2);
        inOrder.verify(taxiiStatusDao).updateOrAdd(expectedFeed);
    }

    @Test
    public void handleCommunicationError() throws Exception {
        when(request.execute()).thenThrow(IOException.class);
        task.run();
        verify(requestFactory).createPollRequest(anyString(), any(TaxiiStatus.Feed.class));
        verify(request).execute();
        verifyZeroInteractions(responseTransformer);
        verifyLog(mockAppender, "HTTP connection problem occured");
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(0L));
    }

    @Test
    public void handleInvalidResponseError() throws Exception {
        when(request.execute()).thenReturn(response);
        doThrow(new Exception("Dummy response")).when(responseTransformer).transform(response);
        task.run();
        verify(requestFactory).createPollRequest(anyString(), any(TaxiiStatus.Feed.class));
                verify(request).execute();
        verifyLog(mockAppender, "Error");
        assertThat(statistics.getPolls(), is(1L));
        assertThat(statistics.getErrors(), is(1L));
    }

    @Test
    public void exceedMaxConnectionAttempts() throws Exception {
        when(request.execute()).thenThrow(new ConnectTimeoutException("error"));
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        when(taxiiStatusDao.find("my-collection")).thenReturn(feed);
        task.run();
        assertThat(feed.getIoErrorCount(), is(1));
        task.run();
        assertThat(feed.getIoErrorCount(), is(2));
        verifyLog(mockAppender, "HTTP connection problem occured");
        assertThat(statistics.getErrors(), is(0L));
        task.run();
        assertThat(feed.getIoErrorCount(), is(3));
        verifyLog(mockAppender, "Error");
        assertThat(statistics.getErrors(), is(1L));
    }

}
