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

import org.dellroad.stuff.pobj.PersistentObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.datatype.DatatypeFactory;

import java.lang.management.ManagementFactory;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;



@ContextConfiguration(classes = AdapterConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdapterConfigurationTest {

    @Autowired
    private AdapterConfiguration adapterCfg;

    @Autowired
    private PersistentObject<TaxiiStatus> taxiiStatusPersistent;

    @Autowired
    private DatatypeFactory datatypeFactory;

    @Test
    public void applyConfiguration() throws Exception {
        assertThat(adapterCfg, notNullValue());
    }

    @Test
    public void writeReadTaxiiStatusWithPersistentObject() throws Exception {
        
        // write
        TaxiiStatus writeRoot = new TaxiiStatus();
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        feed.setName("my");
        feed.setLastUpdate(datatypeFactory.newXMLGregorianCalendar("2000-01-02T03:04:05.006+07:00"));
        writeRoot.getFeed().add(feed);
        taxiiStatusPersistent.setRoot(writeRoot);
        
        //read
        TaxiiStatus readRoot = taxiiStatusPersistent.getRoot();
        assertThat(writeRoot, not(sameInstance(readRoot)));
        assertThat(readRoot.getFeed().get(0), is(feed));
    }

    @Test
    public void jmxStatisticsRegistered() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.cisco.cta.taxii:component=taxii-log-adapter,type=statistics");
        assertTrue(mbs.isRegistered(name));
    }
}
