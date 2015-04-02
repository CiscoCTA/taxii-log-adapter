package com.cisco.cta.taxii.adapter;


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
