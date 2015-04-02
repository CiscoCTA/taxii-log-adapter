package com.cisco.cta.taxii.adapter.persistence;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.xml.datatype.DatatypeFactory;

import org.dellroad.stuff.pobj.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersistenceConfigurationTest {

    @Autowired
    private PersistentObject<TaxiiStatus> taxiiStatusPersistent;

    @Before
    public void setUp() throws Exception {
        
    }

    @Test
    public void writeReadTaxiiStatusWithPersistentObject() throws Exception {
        
        // write
        TaxiiStatus writeRoot = new TaxiiStatus();
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        feed.setName("my");
        feed.setLastUpdate(DatatypeFactory.newInstance().newXMLGregorianCalendar("2000-01-02T03:04:05.006+07:00"));
        writeRoot.getFeed().add(feed);
        taxiiStatusPersistent.setRoot(writeRoot);
        
        //read
        TaxiiStatus readRoot = taxiiStatusPersistent.getRoot();
        assertThat(writeRoot, not(sameInstance(readRoot)));
        assertThat(readRoot.getFeed().get(0), is(feed));
    }

}
