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

    private static final String MORE_ATTRIBUTE = "more";

    private static final String RESULT_ID_ATTRIBUTE = "result_id";

    private static final String RESULT_PART_NUMBER_ATTRIBUTE = "result_part_number";

    private static final QName POLL_RESPONSE = new QName(
            TAXII_NAMESPACE_URI,
            "Poll_Response");

    private static final QName INCLUSIVE_END_TIMESTAMP = new QName(
            TAXII_NAMESPACE_URI,
            "Inclusive_End_Timestamp");

    private static final QName CONTENT_BLOCK = new QName(
            TAXII_NAMESPACE_URI,
            "Content_Block");

    private final DatatypeFactory datatypeFactory;

    private Boolean more;
    private String resultId;
    private Integer resultPartNumber;

    private XMLGregorianCalendar inclusiveEndTime = null;
    private State state = State.ROOT;

    private enum State {ROOT, BEFORE_TIMESTAMP, TIMESTAMP, AFTER_TIMESTAMP, CONTENT, NOT_POLL_RESPONSE}
    
    public TaxiiPollResponseReader(XMLStreamReader xmlReader, DatatypeFactory datatypeFactory) {
        super(xmlReader);
        this.datatypeFactory = datatypeFactory;
    }

    /**
     * This must be invoked after all events were read.
     *
     * @return more attribute parsed from the TAXII Poll_Response element.
     */
    public Boolean isMore() {
        return more;
    }

    /**
     * This must be invoked after all events were read.
     *
     * @return result_id attribute parsed from the TAXII Poll_Response element.
     */
    public String getResultId() {
        return resultId;
    }

    /**
     * This must be invoked after all events were read.
     *
     * @return result_part_numbner attribute parsed from the TAXII Poll_Response element.
     */
    public Integer getResultPartNumber() {
        return resultPartNumber;
    }

    /**
     * This must be invoked after all events were read.
     * 
     * @return Inclusive_End_Timestamp attribute parsed from the TAXII poll response.
     */
    public XMLGregorianCalendar getInclusiveEndTime() {
        return inclusiveEndTime;
    }

    /**
     * Poll response flag.
     * 
     * @return True when the response type is poll response.
     */
    public boolean isPollResponse() {
        return state != State.NOT_POLL_RESPONSE;
    }

    @Override
    public int next() throws XMLStreamException {
        int result = super.next();
        switch (state) {
        case ROOT:
            if (isStartElement()) {
                if (getName().equals(POLL_RESPONSE)) {
                    state = State.BEFORE_TIMESTAMP;
                    more = getAttributeValue("", MORE_ATTRIBUTE) != null ? Boolean.valueOf(getAttributeValue("", MORE_ATTRIBUTE)) : null;
                    resultId = getAttributeValue("", RESULT_ID_ATTRIBUTE) != null ? getAttributeValue("", RESULT_ID_ATTRIBUTE) : null;
                    resultPartNumber = getAttributeValue("", RESULT_PART_NUMBER_ATTRIBUTE) != null ? Integer.valueOf(getAttributeValue("", RESULT_PART_NUMBER_ATTRIBUTE)) : null;
                } else {
                    state = State.NOT_POLL_RESPONSE;
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
        default: // do nothing
        }
        return result;
    }

}
