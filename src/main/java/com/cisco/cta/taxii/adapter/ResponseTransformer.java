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

import org.springframework.http.client.ClientHttpResponse;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Transforms TAXII responses.
 */
public class ResponseTransformer {
    private final Templates templates;
    private final Writer logWriter;
    private final TaxiiPollResponseReaderFactory readerFactory;

    public ResponseTransformer(
            Templates templates,
            Writer logWriter,
            TaxiiPollResponseReaderFactory readerFactory) {
        this.templates = templates;
        this.logWriter = logWriter;
        this.readerFactory = readerFactory;
    }


    /**
     * Transforms TAXII response.
     * 
     * @param resp HTTP response
     * @return TaxiiPollResponse if valid TAXII poll response was returned
     * @throws Exception When any error occurs.
     */
    public TaxiiPollResponse transform(ClientHttpResponse resp) throws Exception {
        if (resp.getRawStatusCode() == HTTP_OK) {
            try (InputStream body = resp.getBody()) {
                TaxiiPollResponseReader responseReader = readerFactory.create(body);
                Transformer transformer = templates.newTransformer();
                transformer.transform(new StAXSource(responseReader), new StreamResult(logWriter));
                return responseReader.getResponse();
            }
        } else {
            throw new IOException("HTTP response status " + resp.getRawStatusCode() + ":" + resp.getStatusText());
        }
    }

}
