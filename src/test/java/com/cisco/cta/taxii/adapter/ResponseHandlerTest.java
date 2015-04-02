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

import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpResponse;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.cisco.cta.taxii.adapter.ResponseHandler;
import com.cisco.cta.taxii.adapter.TaxiiPollResponseReader;
import com.cisco.cta.taxii.adapter.TaxiiPollResponseReaderFactory;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;

public class ResponseHandlerTest {

    private ResponseHandler responseHandler;

    @Mock
    private TaxiiPollResponseReaderFactory readerFactory;

    @Mock
    private Writer logWriter;

    @Mock
    private TaxiiStatusDao taxiiStatusDao;

    @Mock
    private Templates templates;

    @Mock
    private ClientHttpResponse resp;
    
    @Mock
    private InputStream body;
    
    @Mock
    private Transformer transformer;

    @Mock(answer=RETURNS_MOCKS)
    private TaxiiPollResponseReader responseReader;

    private DatatypeFactory datatypeFactory;

    private Clock clock;

    private Instant now;

    @Before
    public void setUp() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        now = Instant.parse("2000-01-02T03:04:05.006Z");
        clock = Clock.fixed(now, ZoneId.systemDefault());
        MockitoAnnotations.initMocks(this);
        when(resp.getBody()).thenReturn(body);
        when(templates.newTransformer()).thenReturn(transformer);
        when(readerFactory.create(body)).thenReturn(responseReader);
        when(responseReader.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);
        responseHandler = new ResponseHandler(templates, logWriter, taxiiStatusDao, readerFactory, datatypeFactory, clock);
    }

    @Test
    public void transformResponse() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        responseHandler.handle("my-feed", resp);
        verify(transformer).transform(isExpectedXmlSource(), isExpectedOutputTarget());
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsNotProvided() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        when(responseReader.isPollResponse()).thenReturn(true);
        when(responseReader.getInclusiveEndTime()).thenReturn(null);
        responseHandler.handle("my-feed", resp);
        verify(taxiiStatusDao).update("my-feed", datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00"));
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsProvided() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        when(responseReader.isPollResponse()).thenReturn(true);
        XMLGregorianCalendar endTime = datatypeFactory.newXMLGregorianCalendar("2014-01-02T03:04:05.006+00:00");
        when(responseReader.getInclusiveEndTime()).thenReturn(endTime);
        responseHandler.handle("my-feed", resp);
        verify(taxiiStatusDao).update("my-feed", endTime);
    }

    @Test
    public void handleNonPollResponseMessage() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        when(responseReader.isPollResponse()).thenReturn(false);
        responseHandler.handle("my-feed", resp);
        verify(transformer).transform(isExpectedXmlSource(), isExpectedOutputTarget());
        verifyZeroInteractions(taxiiStatusDao);
    }

    @Test(expected=IOException.class)
    public void reportErrorHttpStatus() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(300);
        responseHandler.handle("my-feed", resp);
        verifyZeroInteractions(readerFactory);
        verifyZeroInteractions(templates);
        verifyZeroInteractions(taxiiStatusDao);
    }

    private Source isExpectedXmlSource() {
        return argThat(new StaxSourceWrappingBodyReader());
    }

    private class StaxSourceWrappingBodyReader extends ArgumentMatcher<Source>{

        @Override
        public boolean matches(Object argument) {
            StAXSource source = (StAXSource) argument;
            TaxiiPollResponseReader sourceReader = (TaxiiPollResponseReader) source.getXMLStreamReader();
            return sourceReader == responseReader;
        }
        
    }

    private Result isExpectedOutputTarget() {
        return argThat(new StreamResultWrappingLogWriter());
    }

    private class StreamResultWrappingLogWriter extends ArgumentMatcher<Result>{

        @Override
        public boolean matches(Object argument) {
            StreamResult result = (StreamResult) argument;
            return result.getWriter() == logWriter;
        }
    }
}
