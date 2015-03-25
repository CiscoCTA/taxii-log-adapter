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

package com.cisco.cta.taxii.adapter;

import java.net.URI;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * {@link ClientHttpRequestFactory} implementation with basic HTTP authentication.
 * This class gets the credentials from {@link TaxiiServiceSettings}.
 */
public class BasicAuthHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private final CredentialsProvider credsProvider;
    private final AuthCache authCache;

    public BasicAuthHttpRequestFactory(TaxiiServiceSettings settings) {

        URL url = settings.getPollEndpoint();
        HttpHost targetHost = new HttpHost(
                url.getHost(),
                url.getPort(),
                url.getProtocol());

        // Create credentials provider for BASIC auth
        credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(targetHost.getHostName(), targetHost.getPort()),
            new UsernamePasswordCredentials(settings.getUsername(), settings.getPassword()));

        // Create auth cache with BASIC scheme
        authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());
    }

    /**
     * Add AuthCache to the execution context to use preemptive authentication.
     */
    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        return context;
    }

}
