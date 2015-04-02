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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.springframework.http.HttpHeaders;


public class HasHeaderMatcher extends FeatureMatcher<HttpHeaders, String> {

    public static Matcher<HttpHeaders> hasAllTaxiiHeaders() {
        return allOf(
            hasHeader("Content-Type", is("application/xml")),
            hasHeader("Accept", is("application/xml")),
            hasHeader("X-Taxii-Accept", is("urn:taxii.mitre.org:message:xml:1.1")),
            hasHeader("X-TAXII-Content-Type", is("urn:taxii.mitre.org:message:xml:1.1")),
            hasHeader("X-Taxii-Protocol", is("urn:taxii.mitre.org:protocol:https:1.0")),
            hasHeader("X-TAXII-Services", is("urn:taxii.mitre.org:services:1.1"))
        );
    }

    public static Matcher<HttpHeaders> hasHeader(String name, Matcher<String> valueMatcher) {
        return new HasHeaderMatcher(name, valueMatcher);
    }


    private final String name;

    public HasHeaderMatcher(String name, Matcher<String> valueMatcher) {
        super(valueMatcher, "expected headers." + name, name);
        this.name = name;
    }

    @Override
    protected String featureValueOf(HttpHeaders headers) {
        return headers.getFirst(name);
    }

}
