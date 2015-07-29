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


import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.cta.taxii.adapter.TaxiiPollResponseReader;

public class TaxiiPollResponseReaderTest {

    private XMLInputFactory inputFactory;
    private DatatypeFactory datatypeFactory;
    private InputStream taxiiPollRespBody;
    private InputStream taxiiPollRespBodyEmpty;

    @Before
    public void setUp() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        inputFactory = XMLInputFactory.newFactory();
        taxiiPollRespBody = TaxiiPollResponseReaderTest.class.getResourceAsStream(
                "/taxii-poll-response-body-initial.xml");
        taxiiPollRespBodyEmpty = TaxiiPollResponseReaderTest.class.getResourceAsStream(
                "/taxii-poll-response-body-empty.xml");
    }

    @After
    public void tearDown() throws Exception {
        taxiiPollRespBody.close();
        taxiiPollRespBodyEmpty.close();
    }

    @Test
    public void recognizePollResponseMessage() throws Exception {
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(taxiiPollRespBody);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
        assertTrue(reader.isPollResponse());
    }

    @Test
    public void recognizeNonPollResponseMessage() throws Exception {
        Reader in = new StringReader("<Status_Message xmlns=\"http://taxii.mitre.org/messages/taxii_xml_binding-1.1\"><Message>ABC</Message></Status_Message>");
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(in);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
        assertFalse(reader.isPollResponse());
    }

    @Test
    public void parseInclusiveEndTime() throws Exception {
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(taxiiPollRespBody);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
        assertThat(reader.getResponse().getInclusiveEndTime(), is(datatypeFactory.newXMLGregorianCalendar("2000-12-24T01:02:03.004+01:00")));
    }

    @Test
    public void parseMissingInclusiveEndTime() throws Exception {
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(taxiiPollRespBodyEmpty);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
        assertThat(reader.getResponse().getInclusiveEndTime(), is(nullValue()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void errorOnInvalidInclusiveEndTime() throws Exception {
        Reader in = new StringReader("<Poll_Response xmlns=\"http://taxii.mitre.org/messages/taxii_xml_binding-1.1\"><Inclusive_End_Timestamp>INVALID</Inclusive_End_Timestamp></Poll_Response>");
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(in);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
    }

    @Test(expected=IllegalStateException.class)
    public void errorOnUnexpectedElementInInclusiveEndTime() throws Exception {
        Reader in = new StringReader("<Poll_Response xmlns=\"http://taxii.mitre.org/messages/taxii_xml_binding-1.1\"><Inclusive_End_Timestamp><UNEXPECTED/></Inclusive_End_Timestamp></Poll_Response>");
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(in);
        TaxiiPollResponseReader reader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        readFully(reader);
    }

    static void readFully(TaxiiPollResponseReader reader) throws XMLStreamException {
        try {
            while (reader.hasNext()) {
                reader.next();
            }
        } finally {
            reader.close();
        }
    }
}
