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

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.MDC;
import org.springframework.http.client.ClientHttpResponse;
import org.threeten.bp.Clock;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.ZoneId;

/**
 * Handles TAXII responses.
 */
public class ResponseHandler {
    
    private final Templates templates;
    private final Writer logWriter;
    private final TaxiiStatusDao taxiiStatusDao;
    private final TaxiiPollResponseReaderFactory readerFactory;
    private final DatatypeFactory datatypeFactory;
    private final Clock clock;


    public ResponseHandler(
            Templates templates,
            Writer logWriter,
            TaxiiStatusDao taxiiStatusDao,
            TaxiiPollResponseReaderFactory readerFactory,
            DatatypeFactory datatypeFactory,
            Clock clock) {
        this.templates = templates;
        this.logWriter = logWriter;
        this.taxiiStatusDao = taxiiStatusDao;
        this.readerFactory = readerFactory;
        this.datatypeFactory = datatypeFactory;
        this.clock = clock;
    }


    /**
     * Handle TAXII response.
     * 
     * @param feed The TAXII feed name, that was sent in the request.
     * @param resp The TAXII poll response.
     * @throws Exception When any error occurs.
     */
    public void handle(String feed, ClientHttpResponse resp) throws Exception {
        if (resp.getRawStatusCode() == HTTP_OK) {
            MDC.put("feed", feed);
            try (InputStream body = resp.getBody()) {
                TaxiiPollResponseReader responseReader = readerFactory.create(body);
                Transformer transformer = templates.newTransformer();
                transformer.transform(new StAXSource(responseReader), new StreamResult(logWriter));
                if (responseReader.isPollResponse()) {
                    taxiiStatusDao.update(feed, lastUpdate(responseReader));
                }
            } finally {
                MDC.clear();
            }
        } else {
            throw new IOException("HTTP response status " + resp.getRawStatusCode() + ":" + resp.getStatusText());
        }
    }


    private XMLGregorianCalendar lastUpdate(TaxiiPollResponseReader responseReader) {
        if (responseReader.getInclusiveEndTime() == null) {
            GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(clock.instant().atZone(ZoneId.systemDefault()));
            return datatypeFactory.newXMLGregorianCalendar(gregorianCal);
        } else {
            return responseReader.getInclusiveEndTime();
        }
    }

}
