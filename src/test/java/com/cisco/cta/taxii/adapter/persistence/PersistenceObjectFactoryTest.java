package com.cisco.cta.taxii.adapter.persistence;

import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
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
        TaxiiServiceSettings settings = new TaxiiServiceSettings();
        settings.setStatusFile(new File("/nonexistent/path"));
        new PersistenceObjectFactory(settings, new TaxiiStatusDelegate(marshaller)).build();
    }

    @Test
    public void buildSuceedsOnCorrectStatusFilePath() throws Exception {
        TaxiiServiceSettings settings = new TaxiiServiceSettings();
        settings.setStatusFile( folder.newFile("taxii-status-"+System.currentTimeMillis()+".tmp"));
        new PersistenceObjectFactory(settings, new TaxiiStatusDelegate(marshaller)).build();
    }

}