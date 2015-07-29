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


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.cta.taxii.adapter.TaxiiPollResponseReader;
import com.cisco.cta.taxii.adapter.TaxiiPollResponseReaderFactory;

public class TaxiiPollResponseReaderFactoryTest {

    private TaxiiPollResponseReaderFactory factory;
    private InputStream taxiiPollRespBody;
    private DatatypeFactory datatypeFactory;

    @Before
    public void setUp() throws Exception {
        factory = new TaxiiPollResponseReaderFactory(XMLInputFactory.newFactory(), DatatypeFactory.newInstance());
        taxiiPollRespBody = TaxiiPollResponseReaderFactoryTest.class.getResourceAsStream(
                "/taxii-poll-response-body-initial.xml");
        datatypeFactory = DatatypeFactory.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        taxiiPollRespBody.close();
    }

    @Test
    public void createReader() throws Exception {
        TaxiiPollResponseReader reader = factory.create(taxiiPollRespBody);
        TaxiiPollResponseReaderTest.readFully(reader);
        assertThat(reader.getResponse().getInclusiveEndTime(), is(datatypeFactory.newXMLGregorianCalendar("2000-12-24T01:02:03.004+01:00")));
    }
}
