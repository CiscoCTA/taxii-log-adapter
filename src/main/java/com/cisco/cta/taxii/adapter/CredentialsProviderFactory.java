package com.cisco.cta.taxii.adapter;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import java.net.URL;

public class CredentialsProviderFactory {

    private TaxiiServiceSettings settings;
    private ProxySettings proxySettings;

    public CredentialsProviderFactory(TaxiiServiceSettings settings, ProxySettings proxySettings) {
        this.settings = settings;
        this.proxySettings = proxySettings;
    }

    private void addProxyCredentials(CredentialsProvider credsProvider) {
        if (proxySettings.getUrl() == null) {
            return ;
        }

        ProxyAuthenticationType authType = proxySettings.getAuthenticationType();

        URL proxyUrl = proxySettings.getUrl();
        HttpHost proxyHost = new HttpHost(
                proxyUrl.getHost(),
                proxyUrl.getPort(),
                proxyUrl.getProtocol());

        Credentials credentials = null;
        if (ProxyAuthenticationType.NONE == authType) {

        } else  if (ProxyAuthenticationType.BASIC == authType) {
            credentials = new UsernamePasswordCredentials(proxySettings.getUsername(), proxySettings.getPassword());
            credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), proxyHost.getPort()), credentials);

        } else if (ProxyAuthenticationType.NTLM == authType) {
            credentials = new NTCredentials(
                    proxySettings.getUsername(),
                    proxySettings.getPassword(),
                    proxySettings.getWorkstation(),
                    proxySettings.getDomain());
            credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), proxyHost.getPort()), credentials);
        }
    }

    private void addTaxiiCredentials(CredentialsProvider credsProvider) {
        URL url = settings.getPollEndpoint();
        HttpHost targetHost = new HttpHost(
                url.getHost(),
                url.getPort(),
                url.getProtocol());
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(settings.getUsername(), settings.getPassword()));
    }

    public CredentialsProvider build() {
        // Create credentials provider for BASIC auth
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        addProxyCredentials(credsProvider);
        addTaxiiCredentials(credsProvider);
        return credsProvider;
    }

}
