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

package com.cisco.cta.taxii.adapter.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;

import java.io.IOException;

public class JsonValidationFilter extends Filter<ILoggingEvent> {

    public static final String JSON_VALIDATION_ERROR = "JsonValidationError";

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String messsage = event.getMessage();
        try {
            mapper.readTree(messsage);
        }catch(IOException e) {
            MDC.put(JSON_VALIDATION_ERROR, e.getMessage());
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

}
