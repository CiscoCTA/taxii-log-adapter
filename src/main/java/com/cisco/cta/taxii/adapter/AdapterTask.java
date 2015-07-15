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

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.threeten.bp.Clock;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

/**
 * Wraps everything what has to be triggered by a scheduler.
 */
public class AdapterTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterTask.class);

    private static final String MESSAGE_ID_PREFIX = "tla-";

    private final RequestFactory requestFactory;
    private final ResponseHandler responseHandler;
    private final List<String> feeds;
    private final AdapterStatistics statistics;

    private final TaxiiStatusDao taxiiStatusDao;
    private final DatatypeFactory datatypeFactory;
    private final Clock clock;

    public AdapterTask(RequestFactory requestFactory, ResponseHandler responseHandler, TaxiiServiceSettings settings, AdapterStatistics statistics, TaxiiStatusDao taxiiStatusDao, DatatypeFactory datatypeFactory, Clock clock) {
        this.requestFactory = requestFactory;
        this.responseHandler = responseHandler;
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
            try {
                downloadFeed(feed);
            } catch (Exception e) {
                statistics.incrementErrors();
                LOG.error("Error while processing feed " + feed, e);
            }
        }
    }

    private void downloadFeed(String feed) throws Exception {
        TaxiiPollResponse response = null;
        do {
            response = poll(feed, response);
        }while(hasPendingResultParts(response));

        if (responseFullyFetched(response)) {
            taxiiStatusDao.update(feed, lastUpdate(response.getInclusiveEndTime()));
        }
    }

    private boolean hasPendingResultParts(TaxiiPollResponse response) {
        return response != null && response.isMore();
    }

    private boolean responseFullyFetched(TaxiiPollResponse response) {
        return response != null && !(response.isMultipart() && response.isMore());
    }

    private TaxiiPollResponse poll(String feed, TaxiiPollResponse previousResponse) throws Exception {
        statistics.incrementPolls();
        try {
            String messageId = createMessageId();
            MDC.put("messageId", messageId);
            ClientHttpRequest request;
            if (previousResponse == null) {
                LOG.trace("creating initial taxii request...");
                request = requestFactory.createInitialRequest(messageId, feed);
            } else {
                LOG.trace("creating fulfillment taxii request - {}", previousResponse.getResultPartNumber() + 1);
                request = requestFactory.createFulfillmentRequest(messageId, feed, previousResponse.getResultId(), previousResponse.getResultPartNumber() + 1);
            }
            try (ClientHttpResponse resp = request.execute()) {
                return responseHandler.handle(feed, resp);
            }
        } finally{
            MDC.clear();
        }
    }

    private XMLGregorianCalendar lastUpdate(XMLGregorianCalendar inclusiveEndTimestamp) {
        if (inclusiveEndTimestamp == null) {
            GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(clock.instant().atZone(ZoneId.systemDefault()));
            return datatypeFactory.newXMLGregorianCalendar(gregorianCal);
        } else {
            return inclusiveEndTimestamp;
        }
    }

}
