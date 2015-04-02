package com.cisco.cta.taxii.adapter;

import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

public class HttpClientFactoryTest {

    @Mock
    private ProxySettings proxySettings;

    private HttpClientFactory httpClientFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        httpClientFactory = new HttpClientFactory(proxySettings);
    }

    @Test
    public void create() throws MalformedURLException {
        when(proxySettings.getUrl()).thenReturn(new URL("http://url"));
        HttpClient httpClient = httpClientFactory.create();
        assertNotNull(httpClient);
        verify(proxySettings, times(2)).getUrl();
    }

}
