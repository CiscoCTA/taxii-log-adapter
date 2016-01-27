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

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data access object to persist consumption status of the TAXII service.
 */
public class TaxiiStatusDao {

    private static final Logger LOG = LoggerFactory.getLogger(TaxiiStatusDao.class);
    
    private final TaxiiStatusFileHandler fileHandler;
    private TaxiiStatus dirtyStatus;

    public TaxiiStatusDao(TaxiiStatusFileHandler fileHandler) {
        this.fileHandler = fileHandler;
        dirtyStatus = fileHandler.load();
        if (dirtyStatus == null) {
            LOG.warn("TAXII status file not found, all feeds will be fully downloaded");
            dirtyStatus = new TaxiiStatus();
        } else {
            LOG.debug("TAXII status file loaded: {}", dirtyStatus);
        }
    }

    public Feed find(String feedName) {
        for (Feed feed : dirtyStatus.getFeed()) {
            if (feed.getName().equals(feedName)) {
                return feed;
            }
        }
        return null;
    }

    public void updateOrAdd(Feed feed) {
        Feed savedFeed = find(feed.getName());
        if (savedFeed != null) {
            savedFeed.setMore(feed.getMore());
            savedFeed.setResultId(feed.getResultId());
            savedFeed.setResultPartNumber(feed.getResultPartNumber());
            savedFeed.setLastUpdate(feed.getLastUpdate());
        } else {
            dirtyStatus.getFeed().add(feed);
        }
        fileHandler.save(dirtyStatus);
    }

}
