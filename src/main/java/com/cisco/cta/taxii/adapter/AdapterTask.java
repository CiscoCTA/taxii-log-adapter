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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Wraps everything what has to be triggered by a scheduler.
 */
public class AdapterTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterTask.class);

    private final RequestFactory requestFactory;
    private final ResponseHandler responseHandler;
    private final List<String> feeds;
    private final AdapterStatistics statistics;

    public AdapterTask(RequestFactory requestFactory, ResponseHandler responseHandler, List<String> feeds, AdapterStatistics statistics) {
        this.requestFactory = requestFactory;
        this.responseHandler = responseHandler;
        this.feeds = feeds;
        this.statistics = statistics;
    }

    /**
     * Invoked by the scheduler.
     */
    public void run() {
        LOG.trace("triggering task...");
        for (String feed : feeds) {
            statistics.incrementPolls();
            try (
                ClientHttpResponse resp = requestFactory.create(feed).execute();
            ) {
                responseHandler.handle(feed, resp);
            } catch (Exception e) {
                statistics.incrementErrors();
                LOG.error("Error while processing feed " + feed, e);
            }
        }
    }
}