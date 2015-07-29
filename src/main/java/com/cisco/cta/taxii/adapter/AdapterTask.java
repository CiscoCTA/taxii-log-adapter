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

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.threeten.bp.Clock;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.UUID;

/**
 * Wraps everything what has to be triggered by a scheduler.
 */
public class AdapterTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterTask.class);

    private static final String MESSAGE_ID_PREFIX = "tla-";

    private final RequestFactory requestFactory;
    private final ResponseTransformer responseTransformer;
    private final List<String> feeds;
    private final AdapterStatistics statistics;
    private final TaxiiStatusDao taxiiStatusDao;
    private final DatatypeFactory datatypeFactory;
    private final Clock clock;

    public AdapterTask(RequestFactory requestFactory, ResponseTransformer responseTransformer, TaxiiServiceSettings settings, AdapterStatistics statistics, TaxiiStatusDao taxiiStatusDao, DatatypeFactory datatypeFactory, Clock clock) {
        this.requestFactory = requestFactory;
        this.responseTransformer = responseTransformer;
        this.feeds = settings.getFeeds();
        this.statistics = statistics;
        this.taxiiStatusDao = taxiiStatusDao;
        this.datatypeFactory = datatypeFactory;
        this.clock = clock;
    }

    private String createMessageId() {
        return MESSAGE_ID_PREFIX + UUID.randomUUID().toString();
    }

    /**
     * Invoked by the scheduler.
     */
    public void run() {
        LOG.trace("triggering task...");
        for (String feed : feeds) {
            downloadFeed(feed);
        }
    }

    private void downloadFeed(String feedName) {
        TaxiiPollResponse response = null;
        do {
            try {
                String messageId = createMessageId();
                MDC.put("messageId", messageId);
                response = poll(messageId, feedName, response);
                TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
                feed.setName(feedName);
                if (response.isMultipart() && response.isMore()) {
                    feed.setMore(response.isMore());
                    feed.setResultId(response.getResultId());
                    feed.setResultPartNumber(response.getResultPartNumber());
                } else {
                    feed.setMore(null);
                    feed.setResultId(null);
                    feed.setResultPartNumber(null);
                }
                feed.setLastUpdate(getLastUpdate(response));
                taxiiStatusDao.updateOrAdd(feed);
            } catch(Exception e) {
                statistics.incrementErrors();
                LOG.error("Error while processing feed " + feedName, e);
                return ;

            } finally {
                MDC.clear();
            }
        }while(hasPendingResultParts(response));
    }

    private XMLGregorianCalendar getLastUpdate(TaxiiPollResponse response) throws Exception {
        if (response.getInclusiveEndTime() != null) {
            return response.getInclusiveEndTime();
        } else {
            throw new Exception("InclusiveEndTime must be present in TAXII Poll Response if the named data collection is a data feed.");
        }
    }

    private boolean hasPendingResultParts(TaxiiPollResponse response) {
        return response != null && response.isMore();
    }

    private TaxiiPollResponse poll(String messageId, String feed, TaxiiPollResponse previousResponse) throws Exception {
        statistics.incrementPolls();
        ClientHttpRequest request;
        if (previousResponse == null) {
            LOG.trace("creating initial taxii request...");
            request = requestFactory.createInitialRequest(messageId, feed);
        } else {
            LOG.trace("creating fulfillment taxii request - {}", previousResponse.getResultPartNumber() + 1);
            request = requestFactory.createFulfillmentRequest(messageId, feed, previousResponse.getResultId(), previousResponse.getResultPartNumber() + 1);
        }
        try (ClientHttpResponse resp = request.execute()) {
            return responseTransformer.transform(feed, resp);
        }
    }

}
