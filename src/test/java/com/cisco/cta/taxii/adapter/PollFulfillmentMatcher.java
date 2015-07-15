package com.cisco.cta.taxii.adapter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PollFulfillmentMatcher extends TypeSafeDiagnosingMatcher<ByteArrayOutputStream> {

    private static final String NS = "http://taxii.mitre.org/messages/taxii_xml_binding-1.1";
    private static final QName MESSAGE_ID = new QName("message_id");
    private static final QName COLLECTION_NAME = new QName("collection_name");
    private static final QName RESULT_ID = new QName("result_id");
    private static final QName RESULT_PART_NUMBER = new QName("result_part_number");
    private static final XMLInputFactory FACTORY = XMLInputFactory.newFactory();

    private final String messageId;
    private final String collection;
    private final String resultId;
    private final String resultPartNumber;

    public static PollFulfillmentMatcher pollFulfillment(String messageId, String collection, String resultId, String resultPartNumber) {
        return new PollFulfillmentMatcher(messageId, collection, resultId, resultPartNumber);
    }

    public PollFulfillmentMatcher(String messageId, String collection, String resultId, String resultPartNumber) {
        this.messageId = messageId;
        this.collection = collection;
        this.resultId = resultId;
        this.resultPartNumber = resultPartNumber;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("TAXII poll fulfillment");
    }

    @Override
    protected boolean matchesSafely(ByteArrayOutputStream out, Description mismatchDescription) {
        try {
            XMLEventReader xmlReader = FACTORY.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
            StartElement pollRequest = xmlReader.nextTag().asStartElement();
            assertThat(pollRequest.getName(), is(new QName(NS, "Poll_Fulfillment")));
            assertThat(pollRequest.getAttributeByName(MESSAGE_ID).getValue(), startsWith("tla-"));
            assertThat(pollRequest.getAttributeByName(COLLECTION_NAME).getValue(), is(collection));
            assertThat(pollRequest.getAttributeByName(RESULT_ID).getValue(), is(resultId));
            assertThat(pollRequest.getAttributeByName(RESULT_PART_NUMBER).getValue(), is(resultPartNumber));
            assertTrue(xmlReader.nextTag().isEndElement());
            return true;
        } catch (Throwable e) {
            mismatchDescription.appendText(e.getMessage() + '\n' + new String(out.toByteArray()));
            return false;
        }
    }

}