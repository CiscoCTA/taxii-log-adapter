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

package com.cisco.cta.taxii.adapter.status;


import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.SpringDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.google.common.collect.ImmutableMap;

public class TaxiiStatusDaoTest {
    
    private TaxiiStatusDao dao;
    private PersistentObject<TaxiiStatus> persistentObject;
    private String feedName;
    private XMLGregorianCalendar lastUpdate;

    @Before
    public void setUp() throws Exception {
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
        feedName = UUID.randomUUID().toString();
        lastUpdate = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar("2000-12-24T01:02:03.004+00:00");
    }

    @After
    public void tearDown() throws Exception {
        persistentObject.stop();
    }

    @Test
    public void saveLoadInSameInstance() throws Exception {
        dao.update(feedName, lastUpdate);
        assertThat(dao.getLastUpdate(feedName), is(lastUpdate));
    }

    @Test
    public void saveLoadInDifferentInstances() throws Exception {
        dao.update(feedName, lastUpdate);
        persistentObject.stop();
        persistentObject.start();
        dao = new TaxiiStatusDao(persistentObject);
        assertThat(dao.getLastUpdate(feedName), is(lastUpdate));
    }

    @Test
    public void initializeNewFeedWithNull() throws Exception {
        assertThat(dao.getLastUpdate(feedName), is(nullValue()));
    }

}
