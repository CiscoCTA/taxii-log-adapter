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

import java.io.InputStream;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Factory to create {@link TaxiiPollResponseReader} instances.
 */
public class TaxiiPollResponseReaderFactory {

    private final XMLInputFactory inputFactory;
    private final DatatypeFactory datatypeFactory;

    public TaxiiPollResponseReaderFactory(XMLInputFactory inputFactory, DatatypeFactory datatypeFactory) {
        this.inputFactory = inputFactory;
        this.datatypeFactory = datatypeFactory;
    }

    /**
     * Create a reader for a TAXII poll response.
     *
     * @param body {@link InputStream} to read from. 
     * @return The {@link TaxiiPollResponseReader} instance.
     * @throws XMLStreamException When reading from body fails.
     */
    public TaxiiPollResponseReader create(InputStream body) throws XMLStreamException {
        XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(body);
        TaxiiPollResponseReader responseReader = new TaxiiPollResponseReader(xmlReader, datatypeFactory);
        return responseReader;
    }

}
