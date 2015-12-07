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

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.common.collect.ImmutableList;

/**
 * Appends required HTTP headers to a TAXII request.
 */
public class HttpHeadersAppender {

    public void appendTo(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_XML));
        headers.set("X-Taxii-Accept", "urn:taxii.mitre.org:message:xml:1.1");
        headers.set("X-TAXII-Content-Type", "urn:taxii.mitre.org:message:xml:1.1");
        headers.set("X-Taxii-Protocol", "urn:taxii.mitre.org:protocol:https:1.0");
        headers.set("X-TAXII-Services", "urn:taxii.mitre.org:services:1.1");
        headers.set("User-Agent", "taxii-log-adapter-" + Version.getImplVersion());
    }

}
