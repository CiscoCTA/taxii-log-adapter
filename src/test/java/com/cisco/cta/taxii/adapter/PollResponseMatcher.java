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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class PollResponseMatcher extends TypeSafeDiagnosingMatcher<ByteArrayOutputStream> {

    private static final String NS = "http://taxii.mitre.org/messages/taxii_xml_binding-1.1";
    private static final QName MESSAGE_ID = new QName("message_id");
    private static final QName COLLECTION_NAME = new QName("collection_name");
    private static final XMLInputFactory FACTORY = XMLInputFactory.newFactory();

    private final String collection;
    private final String begin;
    
    public static PollResponseMatcher initialPollRequest(String collection) {
        return new PollResponseMatcher(collection, null);
    }

    public static PollResponseMatcher nextPollRequest(String collection, String begin) {
        return new PollResponseMatcher(collection, begin);
    }

    public PollResponseMatcher(String collection, String begin) {
        this.collection = collection;
        this.begin = begin;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("TAXII poll request");
    }

    @Override
    protected boolean matchesSafely(ByteArrayOutputStream out, Description mismatchDescription) {
        try {
            XMLEventReader xmlReader = FACTORY.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
            StartElement pollRequest = xmlReader.nextTag().asStartElement();
            assertThat(pollRequest.getName(), is(new QName(NS, "Poll_Request")));
            assertThat(pollRequest.getAttributeByName(MESSAGE_ID).getValue(), startsWith("tla-"));
            assertThat(pollRequest.getAttributeByName(COLLECTION_NAME).getValue(), is(collection));
            if (begin != null) {
                assertThat(xmlReader.nextTag().asStartElement().getName(), is(new QName(NS, "Exclusive_Begin_Timestamp")));
                assertThat(xmlReader.getElementText(), is(begin));
            }
            assertThat(xmlReader.nextTag().asStartElement().getName(), is(new QName(NS, "Poll_Parameters")));
            assertThat(xmlReader.nextTag().asStartElement().getName(), is(new QName(NS, "Response_Type")));
            assertThat(xmlReader.getElementText(), is("FULL"));
            assertThat(xmlReader.nextTag().asEndElement().getName(), is(new QName(NS, "Poll_Parameters")));
            assertThat(xmlReader.nextTag().asEndElement().getName(), is(new QName(NS, "Poll_Request")));
            assertTrue(xmlReader.nextEvent().isEndDocument());
            return true;
        } catch (Throwable e) {
            mismatchDescription.appendText(e.getMessage() + '\n' + new String(out.toByteArray()));
            return false;
        }
    }

}
