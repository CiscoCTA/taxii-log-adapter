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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * {@link XMLStreamReader} implementation for reading a single TAXII poll response.
 * This class reads TAXII poll response attributes from an underlying reader while
 * providing the same data to its client (e.g. XSL transformation).
 */
public class TaxiiPollResponseReader extends StreamReaderDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(TaxiiPollResponseReader.class);

    private static final String TAXII_NAMESPACE_URI = "http://taxii.mitre.org/messages/taxii_xml_binding-1.1";

    private static final String STATUS_TYPE_ATTRIBUTE = "status_type";

    private static final String MORE_ATTRIBUTE = "more";

    private static final String RESULT_ID_ATTRIBUTE = "result_id";

    private static final String RESULT_PART_NUMBER_ATTRIBUTE = "result_part_number";

    private static final QName POLL_RESPONSE = new QName(
            TAXII_NAMESPACE_URI,
            "Poll_Response");

    private static final QName STATUS_MESSAGE = new QName(
            TAXII_NAMESPACE_URI,
            "Status_Message");

    private static final QName MESSAGE = new QName(
            TAXII_NAMESPACE_URI,
            "Message");

    private static final QName INCLUSIVE_END_TIMESTAMP = new QName(
            TAXII_NAMESPACE_URI,
            "Inclusive_End_Timestamp");

    private static final QName CONTENT_BLOCK = new QName(
            TAXII_NAMESPACE_URI,
            "Content_Block");

    private final DatatypeFactory datatypeFactory;

    private State state = State.ROOT;

    private enum State {ROOT, BEFORE_TIMESTAMP, TIMESTAMP, AFTER_TIMESTAMP, CONTENT, STATUS_MESSAGE, STATUS_MESSAGE_TEXT, OTHER}

    private String statusType;
    private String statusMessage;
    private Boolean more;
    private String resultId;
    private Integer resultPartNumber;
    private XMLGregorianCalendar inclusiveEndTime;

    public TaxiiPollResponseReader(XMLStreamReader xmlReader, DatatypeFactory datatypeFactory) {
        super(xmlReader);
        this.datatypeFactory = datatypeFactory;
    }

    /**
     * Poll response flag.
     * 
     * @return True when the response type is poll response.
     */
    public boolean isPollResponse() {
        return state != State.STATUS_MESSAGE && state != State.STATUS_MESSAGE_TEXT && state != State.OTHER;
    }

    /**
     * This must be invoked after all events were read.
     *
     * @return TaxiiPollResponse
     * @throws TaxiiStatusException in case of taxii status message
     */
    public TaxiiPollResponse getResponse() throws  TaxiiStatusException {
        if (isPollResponse()) {
            return TaxiiPollResponse.builder()
                    .multipart(resultId != null)
                    .more(more != null && more)
                    .resultId(resultId)
                    .resultPartNumber(resultPartNumber)
                    .inclusiveEndTime(inclusiveEndTime)
                    .build();
        } else {
            throw new TaxiiStatusException(statusType, statusMessage);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttributeValue(String attributeName, Class<T> clazz) {
        String value = getAttributeValue("", attributeName);
        if (value == null) {
            return null;
        }
        if (clazz.equals(String.class)) {
            return (T)value;
        } else if (clazz.equals(Integer.class)) {
            return (T)Integer.valueOf(value);
        } else if (clazz.equals(Boolean.class)) {
            return (T)Boolean.valueOf(value);
        } else {
            throw new RuntimeException("Unsupported attribute type: " + clazz.getName());
        }
    }

    @Override
    public int next() throws XMLStreamException {
        int result = super.next();
        switch (state) {
            case ROOT:
                if (isStartElement()) {
                    if (getName().equals(POLL_RESPONSE)) {
                        state = State.BEFORE_TIMESTAMP;
                        more = getAttributeValue(MORE_ATTRIBUTE, Boolean.class);
                        resultId = getAttributeValue(RESULT_ID_ATTRIBUTE, String.class);
                        resultPartNumber = getAttributeValue(RESULT_PART_NUMBER_ATTRIBUTE, Integer.class);
                    } else if (getName().equals(STATUS_MESSAGE)) {
                        statusType = getAttributeValue(STATUS_TYPE_ATTRIBUTE, String.class);
                        state = State.STATUS_MESSAGE;
                    } else {
                        state = State.OTHER;
                    }
                }
                break;
            case BEFORE_TIMESTAMP:
                if (isStartElement()) {
                    if (getName().equals(INCLUSIVE_END_TIMESTAMP)) {
                        state = State.TIMESTAMP;
                    } else if (getName().equals(CONTENT_BLOCK)) {
                        state = State.CONTENT;
                    }
                }
                break;
            case TIMESTAMP:
                state = State.AFTER_TIMESTAMP;
                if (isCharacters()) {
                    inclusiveEndTime = datatypeFactory.newXMLGregorianCalendar(getText());
                } else {
                    throw new IllegalStateException("Expected time value, but found " + getText());
                }
                break;
            case STATUS_MESSAGE:
                if (isStartElement() && getName().equals(MESSAGE)) {
                    state = State.STATUS_MESSAGE_TEXT;
                }
                break;
            case STATUS_MESSAGE_TEXT:
                if (isCharacters()) {
                    statusMessage = getText();
                    state = State.OTHER;
                }
            default: // do nothing
        }
        return result;
    }

}
