package com.cisco.cta.taxii.adapter.persistence;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;

public class PersistenceObjectFactoryTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Mock
    private Jaxb2Marshaller marshaller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = RuntimeException.class)
    public void buildFailsOnWrongStatusFilePath() throws Exception {
        File file = new File("/nonexistent/path");
        new PersistenceObjectFactory(file, new TaxiiStatusDelegate(marshaller)).build();
    }

    @Test
    public void buildSuceedsOnCorrectStatusFilePath() throws Exception {
        File file = folder.newFile("taxii-status-"+System.currentTimeMillis()+".tmp");
        new PersistenceObjectFactory(file, new TaxiiStatusDelegate(marshaller)).build();
    }

}