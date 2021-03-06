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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Wraps everything what has to be triggered by a scheduler.
 */
public class AdapterTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterTask.class);

    private static final String MESSAGE_ID_PREFIX = "tla-";

    private static final Integer MAX_HTTP_CONNECTION_ATTEMPTS = 3;

    private final RequestFactory requestFactory;
    private final ResponseTransformer responseTransformer;
    private final List<String> feeds;
    private final AdapterStatistics statistics;
    private final TaxiiStatusDao taxiiStatusDao;

    public AdapterTask(RequestFactory requestFactory, ResponseTransformer responseTransformer, TaxiiServiceSettings settings, AdapterStatistics statistics, TaxiiStatusDao taxiiStatusDao) {
        this.requestFactory = requestFactory;
        this.responseTransformer = responseTransformer;
        this.feeds = settings.getFeeds();
        this.statistics = statistics;
        this.taxiiStatusDao = taxiiStatusDao;
    }

    private String createMessageId() {
        return MESSAGE_ID_PREFIX + UUID.randomUUID().toString();
    }

    /**
     * Invoked by the scheduler.
     */
    @Override
    public void run() {
        LOG.trace("triggering task...");
        feeds.parallelStream().forEach(this::downloadFeed);
    }

    private void downloadFeed(String feedName) {
        try {
            MDC.put("feed", feedName);
            TaxiiStatus.Feed feed = taxiiStatusDao.find(feedName);
            if (feed == null) {
                feed = new TaxiiStatus.Feed();
                feed.setName(feedName);
            }
            boolean more;
            do {
                more = pollAndUpdateFeed(feed);
                taxiiStatusDao.updateOrAdd(feed);
            } while (more);
        } catch(Exception e) {
            statistics.incrementErrors();
            LOG.error(String.format("Error while processing feed %s", feedName), e);
        } finally {
            MDC.clear();
        }
    }

    private boolean pollAndUpdateFeed(TaxiiStatus.Feed feed) throws Exception {
        try {
            String messageId = createMessageId();
            MDC.put("messageId", messageId);
            TaxiiPollResponse response = poll(messageId, feed);
            if (response.getResultId() != null && response.isMore()) {
                feed.setMore(response.isMore());
                feed.setResultId(response.getResultId());
                feed.setResultPartNumber(response.getResultPartNumber());
            } else {
                feed.setMore(null);
                feed.setResultId(null);
                feed.setResultPartNumber(null);
            }
            feed.setIoErrorCount(null);
            feed.setLastUpdate(getLastUpdate(response));
            return response.isMore();

        } catch (IOException e) {
            handleIOError(feed, e);
            return false;
        }
    }

    private void handleIOError(TaxiiStatus.Feed feed, Throwable e) {
        Integer ioErrorCount = feed.getIoErrorCount();
        if (ioErrorCount == null) {
            ioErrorCount = 1;
        } else {
            ioErrorCount += 1;
        }
        feed.setIoErrorCount(ioErrorCount);
        taxiiStatusDao.updateOrAdd(feed);
        if (ioErrorCount >= MAX_HTTP_CONNECTION_ATTEMPTS) {
            throw new RuntimeException("HTTP connection problem, number of retries exceeded.", e);
        } else {
            LOG.warn("HTTP connection problem, the request will be retried.", e);
        }
    }

    private XMLGregorianCalendar getLastUpdate(TaxiiPollResponse response) {
        if (response.getInclusiveEndTime() != null) {
            return response.getInclusiveEndTime();
        } else {
            throw new RuntimeException("InclusiveEndTime must be present in TAXII Poll Response if the named data collection is a data feed.");
        }
    }

    @SuppressFBWarnings(
            value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "try with resources - false positive if compiled with Java 11"
    )
    private TaxiiPollResponse poll(String messageId, TaxiiStatus.Feed lastSuccessfulFeed) throws Exception {
        statistics.incrementPolls();
        ClientHttpRequest request;
        if (lastSuccessfulFeed.getResultPartNumber() == null) {
            LOG.trace("creating initial taxii request...");
            request = requestFactory.createPollRequest(messageId, lastSuccessfulFeed);
        } else {
            LOG.trace("creating fulfillment taxii request - {}", lastSuccessfulFeed.getResultPartNumber() + 1);
            request = requestFactory.createFulfillmentRequest(
                    messageId, lastSuccessfulFeed, lastSuccessfulFeed.getResultId(), lastSuccessfulFeed.getResultPartNumber() + 1);
        }
        try (ClientHttpResponse resp = request.execute()) {
            return responseTransformer.transform(resp);
        }
    }

}
