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

import static com.cisco.cta.taxii.adapter.PollRequestMatcher.initialPollRequest;
import static com.cisco.cta.taxii.adapter.PollRequestMatcher.nextPollRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

@RunWith(MockitoJUnitRunner.class)
public class HttpBodyWriterTest {

    @Mock
    private TaxiiStatusDao taxiiStatusDao;

    @InjectMocks
    private HttpBodyWriter writer;

    @Spy
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void writeInitialPollRequest() throws Exception {
        writer.write("tla-123", "my-feed", out);
        assertThat(out, is(initialPollRequest("tla-123", "my-feed")));
        verify(out).close();
    }

    @Test
    public void writeNextPollRequest() throws Exception {
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2015-01-01T00:00:00");
        feed.setLastUpdate(cal);
        when(taxiiStatusDao.find("my-feed")).thenReturn(feed);
        writer.write("tla-123", "my-feed", out);
        assertThat(out, is(nextPollRequest("tla-123", "my-feed", "2015-01-01T00:00:00")));
        verify(out).close();
    }

}
