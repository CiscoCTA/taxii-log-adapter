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

package com.cisco.cta.taxii.adapter.persistence;

import com.cisco.cta.taxii.adapter.TaxiiPollResponse;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus.Feed;
import org.dellroad.stuff.pobj.PersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.*;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;


/**
 * Data access object to persist consumption status of the TAXII service.
 */
public class TaxiiStatusDao {

    private static final Logger LOG = LoggerFactory.getLogger(TaxiiStatusDao.class);
    
    private final PersistentObject<TaxiiStatus> pobj;
    private final DatatypeFactory datatypeFactory;
    private final Clock clock;
    private TaxiiStatus dirtyStatus;

    public TaxiiStatusDao(PersistentObject<TaxiiStatus> pobj, DatatypeFactory datatypeFactory, Clock clock) {
        this.pobj = pobj;
        this.datatypeFactory = datatypeFactory;
        this.clock = clock;
        dirtyStatus = pobj.getRoot();
        if (dirtyStatus == null) {
            LOG.warn("TAXII status file not found, all feeds will be fully downloaded");
            dirtyStatus = new TaxiiStatus();
        } else {
            LOG.debug("TAXII status file loaded: {}", dirtyStatus);
        }
    }

    public void update(String feedName, TaxiiPollResponse response) {
        Feed feed = findOrAdd(feedName);
        feed.setLastUpdate(lastUpdate(response.getInclusiveEndTime()));
        if (response.isMultipart() && response.isMore()) {
            feed.setMore(response.isMore());
            feed.setResultId(response.getResultId());
            feed.setResultPartNumber(response.getResultPartNumber());
        } else {
            feed.setMore(null);
            feed.setResultId(null);
            feed.setResultPartNumber(null);
        }
        pobj.setRoot(dirtyStatus);
    }

    Feed findOrAdd(String feedName) {
        for (Feed feed : dirtyStatus.getFeed()) {
            if (feed.getName().equals(feedName)) {
                return feed;
            }
        }
        Feed feed = new Feed();
        feed.setName(feedName);
        dirtyStatus.getFeed().add(feed);
        return feed;
    }

    public XMLGregorianCalendar getLastUpdate(String feedName) {
        Feed feed = findOrAdd(feedName);
        return feed.getLastUpdate();
    }

    XMLGregorianCalendar instantToXMLGregorianCalendar(Instant instant) {
        GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(instant.atZone(ZoneId.systemDefault()));
        return datatypeFactory.newXMLGregorianCalendar(gregorianCal);
    }

    private XMLGregorianCalendar lastUpdate(XMLGregorianCalendar inclusiveEndTimestamp) {
        if (inclusiveEndTimestamp == null) {
            return instantToXMLGregorianCalendar(clock.instant());
        } else {
            return inclusiveEndTimestamp;
        }
    }

}
