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

package com.cisco.cta.taxii.adapter.httpclient;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;

import static com.cisco.cta.taxii.adapter.PollRequestMatcher.initialPollRequest;
import static com.cisco.cta.taxii.adapter.PollRequestMatcher.nextPollRequest;
import static com.cisco.cta.taxii.adapter.PollFulfillmentMatcher.pollFulfillment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HttpBodyWriterTest {

    @InjectMocks
    private HttpBodyWriter writer;

    @Spy
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private TaxiiStatus.Feed feed;

    private DatatypeFactory datatypeFactory;
    private XMLGregorianCalendar cal;


    @Before
    public void setUp() throws Exception {
        feed = new TaxiiStatus.Feed();
        feed.setName("my-feed");
        datatypeFactory = DatatypeFactory.newInstance();
        cal = datatypeFactory.newXMLGregorianCalendar("2015-01-01T00:00:00");
    }

    @Test
    public void writeInitialPollRequest() throws Exception {
        writer.write("tla-123", feed, out);
        assertThat(out, is(initialPollRequest("tla-123", "my-feed")));
        verify(out).close();
    }

    @Test
    public void writeNextPollRequest() throws Exception {
        feed.setLastUpdate(cal);
        writer.write("tla-123", feed, out);
        assertThat(out, is(nextPollRequest("tla-123", "my-feed", "2015-01-01T00:00:00")));
        verify(out).close();
    }

    @Test
    public void resultPartNumberFormattedCorrectly() throws Exception {
        feed.setLastUpdate(cal);
        writer.write("tla-123", feed, "123#456", 1000, out);
        assertThat(out, is(pollFulfillment("tla-123", "my-feed", "123#456", "1000")));
        verify(out).close();
    }

}
