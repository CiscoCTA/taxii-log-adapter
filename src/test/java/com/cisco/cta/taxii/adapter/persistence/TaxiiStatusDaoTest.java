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


import com.google.common.collect.ImmutableMap;
import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.SpringDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TaxiiStatusDaoTest {
    
    private TaxiiStatusDao dao;
    private PersistentObject<TaxiiStatus> persistentObject;
    private TaxiiStatus.Feed feed;

    private DatatypeFactory datatypeFactory;

    private Instant now;

    @Before
    public void setUp() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        now = Instant.parse("2000-01-02T03:04:05.006Z");
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(TaxiiStatus.class);
        jaxb2Marshaller.setMarshallerProperties(ImmutableMap.of(
                javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true));
        SpringDelegate<TaxiiStatus> delegate = new SpringDelegate<>();
        delegate.setMarshaller(jaxb2Marshaller);
        delegate.setUnmarshaller(jaxb2Marshaller);
        File file = new File("target/TaxiiStatusDaoTest.xml");
        persistentObject = new PersistentObject<>(delegate, file);
        persistentObject.setAllowEmptyStart(true);
        persistentObject.start();
        dao = new TaxiiStatusDao(persistentObject);
        feed = new TaxiiStatus.Feed();
        feed.setMore(true);
        feed.setResultId("1000#2000");
        feed.setResultPartNumber(1);
        GregorianCalendar gregorianCal = DateTimeUtils.toGregorianCalendar(now.atZone(ZoneId.systemDefault()));
        feed.setLastUpdate(datatypeFactory.newXMLGregorianCalendar(gregorianCal));
    }

    @After
    public void tearDown() throws Exception {
        persistentObject.stop();
    }

    @Test
    public void saveAndLoad() {
        feed.setName(UUID.randomUUID().toString());
        dao.updateOrAdd(feed);
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(feed, is(loadedFeed));
    }

    @Test
    public void update() {
        feed.setName(UUID.randomUUID().toString());
        dao.updateOrAdd(feed);
        feed.setResultPartNumber(2);
        dao.updateOrAdd(feed);
        TaxiiStatus.Feed loadedFeed = dao.find(feed.getName());
        assertThat(loadedFeed.getResultPartNumber(), is(2));
    }

}
