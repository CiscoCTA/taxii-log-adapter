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
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;

import static com.cisco.cta.taxii.adapter.PollRequestMatcher.initialPollRequest;
import static com.cisco.cta.taxii.adapter.PollRequestMatcher.nextPollRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpBodyWriterTest {

    @Mock
    private TaxiiStatusDao taxiiStatusDao;

    @InjectMocks
    private HttpBodyWriter writer;

    @Spy
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private TaxiiStatus.Feed feed;

    @org.junit.Before
    public void setUp() throws Exception {
        feed = new TaxiiStatus.Feed();
        feed.setName("my-feed");
    }

    @Test
    public void writeInitialPollRequest() throws Exception {
        writer.write("tla-123", feed, out);
        assertThat(out, is(initialPollRequest("tla-123", "my-feed")));
        verify(out).close();
    }

    @Test
    public void writeNextPollRequest() throws Exception {
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2015-01-01T00:00:00");
        feed.setLastUpdate(cal);
        when(taxiiStatusDao.find("my-feed")).thenReturn(feed);
        writer.write("tla-123", feed, out);
        assertThat(out, is(nextPollRequest("tla-123", "my-feed", "2015-01-01T00:00:00")));
        verify(out).close();
    }

}
