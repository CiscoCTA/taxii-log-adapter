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


import com.cisco.cta.taxii.adapter.YamlFileApplicationContextInitializer;
import org.dellroad.stuff.pobj.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@ContextConfiguration(classes = PersistenceConfiguration.class, initializers = YamlFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TaxiiStatusDaoTest {
    
    @Autowired
    private TaxiiStatusDao dao;

    @Autowired
    private PersistentObject<TaxiiStatus> persistentObject;
    
    private DatatypeFactory datatypeFactory;
    private Instant now;

    @Before
    public void setUp() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        now = Instant.parse("2000-01-02T03:04:05.006Z");
    }

    @Test
    public void initializeNewFeedWithNull() throws Exception {
        assertThat(dao.find(UUID.randomUUID().toString()), nullValue());
    }

    private TaxiiStatus.Feed createNormalFeed() {
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        feed.setName(UUID.randomUUID().toString());
        GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(now.atZone(ZoneId.systemDefault()));
        feed.setLastUpdate(datatypeFactory.newXMLGregorianCalendar(gregorianCal));
        return feed;
    }

    private TaxiiStatus.Feed createMultipartFeed() {
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        feed.setName(UUID.randomUUID().toString());
        feed.setMore(true);
        feed.setResultId("1000#2000");
        feed.setResultPartNumber(1);
        GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(now.atZone(ZoneId.systemDefault()));
        feed.setLastUpdate(datatypeFactory.newXMLGregorianCalendar(gregorianCal));
        return feed;
    }

    @Test
    public void saveLoadInSameInstance() throws Exception {
        TaxiiStatus.Feed feed = createNormalFeed();
        dao.updateOrAdd(feed);
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(feed, is(loadedFeed));
    }

    @Test
    public void saveLoadInDifferentInstances() throws Exception {
        TaxiiStatus.Feed feed = createNormalFeed();
        dao.updateOrAdd(feed);
        persistentObject.stop();
        persistentObject.start();
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(feed, is(loadedFeed));
    }

    @Test
    public void saveLoadMultipartFeed() throws Exception {
        TaxiiStatus.Feed feed = createMultipartFeed();
        dao.updateOrAdd(feed);
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(feed, is(loadedFeed));
    }

    @Test
    public void update() {
        TaxiiStatus.Feed feed = createNormalFeed();
        dao.updateOrAdd(feed);
        feed.setResultPartNumber(2);
        dao.updateOrAdd(feed);
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(loadedFeed.getResultPartNumber(), is(2));
    }

}
