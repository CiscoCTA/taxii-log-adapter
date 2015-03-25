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

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Exposes adapter statistics via a JMX bean.
 * <code>com.sample.taxii:component=taxii-log-adapter,type=statistics</code>
 */
@ManagedResource("com.sample.taxii:component=taxii-log-adapter,type=statistics")
public class AdapterStatistics implements AdapterStatisticsMBean {

    private final AtomicLong pollCounter = new AtomicLong();
    private final AtomicLong errorCounter = new AtomicLong();
    private final AtomicLong logCounter = new AtomicLong();


    @Override
    public Long getPolls() {
        return pollCounter.get();
    }

    @Override
    public Long getLogs() {
        return logCounter.get();
    }

    @Override
    public Long getErrors() {
        return errorCounter.get();
    }

    public void incrementPolls() {
        pollCounter.incrementAndGet();
    }

    public void incrementLogs() {
        logCounter.incrementAndGet();
    }

    public void incrementErrors() {
        errorCounter.incrementAndGet();
    }

}
