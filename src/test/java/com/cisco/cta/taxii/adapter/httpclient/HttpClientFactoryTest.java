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

package com.cisco.cta.taxii.adapter.httpclient;

import com.cisco.cta.taxii.adapter.httpclient.HttpClientFactory;
import com.cisco.cta.taxii.adapter.httpclient.ProxySettings;
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
