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

import com.cisco.cta.taxii.adapter.httpclient.CredentialsProviderFactory;
import com.cisco.cta.taxii.adapter.httpclient.ProxyAuthenticationType;
import com.cisco.cta.taxii.adapter.settings.ProxySettings;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettingsFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

public class CredentialsProviderFactoryTest {

    private TaxiiServiceSettings taxiiSettings;

    @Before
    public void setUp() throws Exception {
        taxiiSettings = TaxiiServiceSettingsFactory.createDefaults();
    }

    @Test
    public void noProxy() throws MalformedURLException {
        ProxySettings proxySettings = new ProxySettings();
        CredentialsProviderFactory factory = new CredentialsProviderFactory(taxiiSettings, proxySettings);
        CredentialsProvider credsProvider = factory.build();
        assertNotNull(credsProvider);
    }

    @Test
    public void proxyWithoutAuthentication() throws MalformedURLException {
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setAuthenticationType(ProxyAuthenticationType.NONE);
        proxySettings.setUrl(new URL("http://localhost:8001/"));
        CredentialsProviderFactory factory = new CredentialsProviderFactory(taxiiSettings, proxySettings);
        CredentialsProvider credsProvider = factory.build();
        assertNotNull(credsProvider);
        Credentials creds = credsProvider.getCredentials(new AuthScope("localhost", 8001));
        assertNull(creds);
    }

    @Test
    public void proxyWithBasicAuth() throws MalformedURLException {
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setAuthenticationType(ProxyAuthenticationType.BASIC);
        proxySettings.setUrl(new URL("http://localhost:8001/"));
        proxySettings.setUsername("tester");
        proxySettings.setPassword("testPass");
        CredentialsProviderFactory factory = new CredentialsProviderFactory(taxiiSettings, proxySettings);
        CredentialsProvider credsProvider = factory.build();
        Credentials creds = credsProvider.getCredentials(new AuthScope("localhost", 8001));
        assertThat(creds.getUserPrincipal().getName(), is("tester"));
        assertThat(creds.getPassword(), is("testPass"));
    }

    @Test
    public void proxyWithNTLMAuth() throws MalformedURLException {
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setAuthenticationType(ProxyAuthenticationType.NTLM);
        proxySettings.setUrl(new URL("http://localhost:8001/"));
        proxySettings.setUsername("tester");
        proxySettings.setPassword("testPass");
        CredentialsProviderFactory factory = new CredentialsProviderFactory(taxiiSettings, proxySettings);
        CredentialsProvider credsProvider = factory.build();
        Credentials creds = credsProvider.getCredentials(new AuthScope("localhost", 8001));
        assertThat(creds.getUserPrincipal().getName(), is("tester"));
        assertThat(creds.getPassword(), is("testPass"));
    }

}
