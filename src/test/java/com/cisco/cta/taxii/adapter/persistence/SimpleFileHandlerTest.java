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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.io.File;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Test;


public class SimpleFileHandlerTest {

    private TaxiiStatusFileHandler handler;
    private File file;

    @Before
    public void setUp() throws Exception {
        file = new File("target/SimpleFileHandlerTest.data");
        PersistenceConfiguration factory = new PersistenceConfiguration();
        handler = new SimpleFileHandler(file, factory.taxiiStatusMarshaller());
    }

    @Test
    public void saveLoad() throws Exception {
        // write
        TaxiiStatus writeRoot = new TaxiiStatus();
        TaxiiStatus.Feed feed = new TaxiiStatus.Feed();
        feed.setName("my");
        feed.setLastUpdate(DatatypeFactory.newInstance().newXMLGregorianCalendar("2000-01-02T03:04:05.006+07:00"));
        writeRoot.getFeed().add(feed);
        handler.save(writeRoot);
        
        //read
        TaxiiStatus readRoot = handler.load();
        assertThat(writeRoot, not(sameInstance(readRoot)));
        assertThat(readRoot.getFeed().get(0), is(feed));
    }

    @Test
    public void missingFileIsEmptyStatus() throws Exception {
        file.delete();
        TaxiiStatus status = handler.load();
        assertThat(status, is(notNullValue()));
    }
}
