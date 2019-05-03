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
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * Writes an HTTP request body to an {@link OutputStream}.
 * The body is a TAXII poll request composed by <a href="http://freemarker.org/">FreeMarker</a>
 */
public class HttpBodyWriter {

    private final Configuration cfg;
    private final Template template;
    private final Template templateFulfillment;
    private final TaxiiStatusDao taxiiStatusDao;

    public HttpBodyWriter(TaxiiStatusDao taxiiStatusDao) throws IOException {
        this.taxiiStatusDao = taxiiStatusDao;
        cfg = new Configuration(new Version(2, 3, 21));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateLoader(new ClassTemplateLoader(HttpBodyWriter.class, "templates"));
        template = cfg.getTemplate("poll-request.ftl");
        templateFulfillment = cfg.getTemplate("poll-fulfillment-request.ftl");
    }

    /**
     * Write the TAXII poll request to an {@link OutputStream}.
     *
     * @param messageId TAXII message id.
     * @param feed The TAXII feed.
     * @param body The {@link OutputStream} to write the body to.
     * @throws Exception When any error occurs.
     */
    public void write(String messageId, TaxiiStatus.Feed feed, OutputStream body) throws Exception {
        try (OutputStreamWriter out = new OutputStreamWriter(body, "UTF-8")) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("messageId", messageId);
            data.put("collection", feed.getName());
            XMLGregorianCalendar lastUpdate = getLastUpdate(feed);
            if (lastUpdate != null) {
                data.put("begin", lastUpdate.toXMLFormat());
            }
            template.process(data, out);
        }
    }

    /**
     * Write the TAXII poll fulfillment request to an {@link OutputStream}.
     *
     * @param messageId        TAXII message id.
     * @param feed             The TAXII feed.
     * @param resultId         result id.
     * @param resultPartNumber result part number.
     * @param body             The {@link OutputStream} to write the body to.
     * @throws Exception When any error occurs.
     */
    public void write(String messageId, TaxiiStatus.Feed feed, String resultId, Integer resultPartNumber, OutputStream body) throws Exception {
        try (OutputStreamWriter out = new OutputStreamWriter(body, "UTF-8")) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("messageId", messageId);
            data.put("collection", feed.getName());
            data.put("resultId", resultId);
            data.put("resultPartNumber", resultPartNumber);
            templateFulfillment.process(data, out);
        }
    }

    private XMLGregorianCalendar getLastUpdate(TaxiiStatus.Feed feed) {
        if (feed != null && feed.getLastUpdate() != null) {
            return feed.getLastUpdate();
        } else {
            return null;
        }
    }

}
