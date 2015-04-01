package com.cisco.cta.taxii.adapter;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;
import java.net.URL;

/**
 * {@link ClientHttpRequestFactory} implementation with basic HTTP authentication.
 * This class gets the credentials from {@link TaxiiServiceSettings}.
 */
public class BasicAuthHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private final CredentialsProvider credsProvider;
    private final AuthCache authCache;

    public BasicAuthHttpRequestFactory(HttpClient httpClient, TaxiiServiceSettings settings, ProxySettings proxySettings, CredentialsProvider credsProvider) {
        super(httpClient);
        this.credsProvider = credsProvider;

        URL url = settings.getPollEndpoint();
        HttpHost targetHost = new HttpHost(
                url.getHost(),
                url.getPort(),
                url.getProtocol());

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
