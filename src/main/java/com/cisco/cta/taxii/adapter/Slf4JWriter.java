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
import org.slf4j.MDC;

import java.io.IOException;
import java.io.Writer;

/**
 * Adapter from {@link Writer} to <a href="http://www.slf4j.org/">SLF4J</a>.
 */
public class Slf4JWriter extends Writer {
    private static final Logger LOG = LoggerFactory.getLogger(Slf4JWriter.class);

    private final Logger logger;
    private final StringBuilder lineBuffer;
    private final AdapterStatistics statistics;

    public Slf4JWriter(Logger logger, AdapterStatistics statistics) {
        lineBuffer = new StringBuilder();
        this.logger = logger;
        this.statistics = statistics;
    }


    @Override
    public Writer append(char c) throws IOException {
        if (c == '\n' && lineBuffer.length() > 0) {
            try {
                logger.info(lineBuffer.toString());
                String validationError = MDC.get(OutputValidationException.MDC_KEY);
                if (validationError != null) {
                    throw new OutputValidationException(validationError);
                }
            } finally {
                MDC.remove(OutputValidationException.MDC_KEY);
            }
            lineBuffer.setLength(0);
            statistics.incrementLogs();
        }
        else {
            lineBuffer.append(c);
        }
        return this;
    }


    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            append(cbuf[i]);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        if (lineBuffer.length() > 0) {
            LOG.error("unfinished {}", lineBuffer);
        }
    }

}
