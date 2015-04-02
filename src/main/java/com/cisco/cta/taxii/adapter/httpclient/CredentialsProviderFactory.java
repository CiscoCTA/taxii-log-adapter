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

import com.cisco.cta.taxii.adapter.TaxiiServiceSettings;
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
        if (proxySettings.getAuthenticationType() == null) {
            throw new IllegalStateException("Both proxy url and proxy authentication type have to be specified.");
        }

        ProxyAuthenticationType authType = proxySettings.getAuthenticationType();

        URL proxyUrl = proxySettings.getUrl();
        HttpHost proxyHost = new HttpHost(
                proxyUrl.getHost(),
                proxyUrl.getPort(),
                proxyUrl.getProtocol());

        Credentials credentials;
        switch(authType) {
            case NONE:
                break;

            case BASIC:
                credentials = new UsernamePasswordCredentials(proxySettings.getUsername(), proxySettings.getPassword());
                credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), proxyHost.getPort()), credentials);
                break;

            case NTLM:
                credentials = new NTCredentials(
                        proxySettings.getUsername(),
                        proxySettings.getPassword(),
                        proxySettings.getWorkstation(),
                        proxySettings.getDomain());
                credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), proxyHost.getPort()), credentials);
                break;

            default:
                throw new IllegalStateException("Unsupported authentication type: " + authType);
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
