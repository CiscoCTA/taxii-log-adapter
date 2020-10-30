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

import org.dellroad.stuff.pobj.PersistentObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PersistenceObjectFactoryTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Mock
    private Jaxb2Marshaller marshaller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildSucceedsOnCorrectStatusFilePath() throws Exception {
        File file = folder.newFile("taxii-status-"+System.currentTimeMillis()+".tmp");
        PersistentObject<TaxiiStatus> persistentObject = new PersistenceObjectFactory(file, new TaxiiStatusDelegate(marshaller)).build();
        assertTrue(persistentObject.isStarted());
    }

}
