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

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpResponse;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ResponseTransformerTest {

    private ResponseTransformer responseTransformer;

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

    private TaxiiStatus.Feed feed;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(resp.getBody()).thenReturn(body);
        when(templates.newTransformer()).thenReturn(transformer);
        when(readerFactory.create(body)).thenReturn(responseReader);
        when(responseReader.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);
        responseTransformer = new ResponseTransformer(templates, logWriter, readerFactory);
        feed = new TaxiiStatus.Feed();
        feed.setName("my-feed");
    }

    @Test
    public void transformResponse() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        responseTransformer.transform(resp);
        verify(transformer).transform(isExpectedXmlSource(), isExpectedOutputTarget());
    }

    @Test
    public void handleNonPollResponseMessage() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(200);
        when(responseReader.isPollResponse()).thenReturn(false);
        responseTransformer.transform(resp);
        verify(transformer).transform(isExpectedXmlSource(), isExpectedOutputTarget());
        verifyZeroInteractions(taxiiStatusDao);
    }

    @Test(expected=IOException.class)
    public void reportErrorHttpStatus() throws Exception {
        when(resp.getRawStatusCode()).thenReturn(300);
        responseTransformer.transform(resp);
        verifyZeroInteractions(readerFactory);
        verifyZeroInteractions(templates);
        verifyZeroInteractions(taxiiStatusDao);
    }

    private Source isExpectedXmlSource() {
        return argThat(new StaxSourceWrappingBodyReader());
    }

    private class StaxSourceWrappingBodyReader implements ArgumentMatcher<Source>{

        @Override
        public boolean matches(Source argument) {
            StAXSource source = (StAXSource) argument;
            TaxiiPollResponseReader sourceReader = (TaxiiPollResponseReader) source.getXMLStreamReader();
            return sourceReader == responseReader;
        }
        
    }

    private Result isExpectedOutputTarget() {
        return argThat(new StreamResultWrappingLogWriter());
    }

    private class StreamResultWrappingLogWriter implements ArgumentMatcher<Result>{

        @Override
        public boolean matches(Result argument) {
            StreamResult result = (StreamResult) argument;
            return result.getWriter() == logWriter;
        }
    }
}
