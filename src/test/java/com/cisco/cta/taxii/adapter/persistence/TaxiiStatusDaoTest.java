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
import com.google.common.collect.ImmutableMap;
import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.SpringDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaxiiStatusDaoTest {
    
    private TaxiiStatusDao dao;
    private PersistentObject<TaxiiStatus> persistentObject;
    private String feedName;
    private TaxiiPollResponse taxiiPollResponse;

    private DatatypeFactory datatypeFactory;

    private Clock clock;

    private Instant now;

    @Before
    public void setUp() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        now = Instant.parse("2000-01-02T03:04:05.006Z");
        clock = Clock.fixed(now, ZoneId.systemDefault());
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
        dao = new TaxiiStatusDao(persistentObject, datatypeFactory, clock);
        feedName = UUID.randomUUID().toString();
        taxiiPollResponse = TaxiiPollResponse.builder()
                .inclusiveEndTime(datatypeFactory.newXMLGregorianCalendar("2000-12-24T01:02:03.004+00:00"))
                .build();
    }

    @After
    public void tearDown() throws Exception {
        persistentObject.stop();
    }

    @Test
    public void saveLoadInSameInstance() throws Exception {
        dao.update(feedName, taxiiPollResponse);
        assertThat(dao.getLastUpdate(feedName), is(taxiiPollResponse.getInclusiveEndTime()));
    }

    @Test
    public void saveLoadInDifferentInstances() throws Exception {
        dao.update(feedName, taxiiPollResponse);
        persistentObject.stop();
        persistentObject.start();
        dao = new TaxiiStatusDao(persistentObject, datatypeFactory, clock);
        assertThat(dao.getLastUpdate(feedName), is(taxiiPollResponse.getInclusiveEndTime()));
    }

    @Test
    public void initializeNewFeedWithNull() throws Exception {
        assertThat(dao.getLastUpdate(feedName), is(nullValue()));
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsNotProvided() throws Exception {
        dao.update(feedName, TaxiiPollResponse.builder().build());
        assertThat(dao.getLastUpdate(feedName), is(dao.instantToXMLGregorianCalendar(clock.instant())));
    }

    @Test
    public void writeLastUpdateWhenEndTimeIsProvided() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        dao.update(feedName, TaxiiPollResponse.builder().inclusiveEndTime(cal).build());
        assertThat(dao.getLastUpdate(feedName), is(cal));
    }

    @Test
    public void verifyStatusFormatForNonMultipartMessage() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        dao.update(feedName, TaxiiPollResponse.builder().multipart(false).inclusiveEndTime(cal).build());
        TaxiiStatus.Feed feed = dao.findOrAdd(feedName);
        assertThat(feed.getMore(), nullValue());
        assertThat(feed.getResultId(), nullValue());
        assertThat(feed.getResultPartNumber(), nullValue());
    }

    @Test
    public void verifyStatusFormatForMultipartMessage() throws Exception {
        XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+00:00");
        dao.update(feedName, TaxiiPollResponse.builder().multipart(true).more(true).resultId("1000#2000").resultPartNumber(5).inclusiveEndTime(cal).build());
        TaxiiStatus.Feed feed = dao.findOrAdd(feedName);
        assertThat(feed.getMore(), is(true));
        assertThat(feed.getResultId(), is("1000#2000"));
        assertThat(feed.getResultPartNumber(), is(5));
    }

}
