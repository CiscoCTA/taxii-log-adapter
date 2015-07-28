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

import com.cisco.cta.taxii.adapter.RequestFactory;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.ProxySettings;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class HttpClientConfiguration {

    @Autowired
    private TaxiiServiceSettings taxiiServiceSettings;

    @Autowired
    private TaxiiStatusDao taxiiStatusDao;

    @Autowired
    private ProxySettings proxySettings;

    @Bean
    public HttpClient httpClient() {
        return new HttpClientFactory(proxySettings).create();
    }

    @Bean
    public CredentialsProvider credentialsProvider() {
        return new CredentialsProviderFactory(taxiiServiceSettings, proxySettings)
                .build();
    }

    @Bean
    public HttpHeadersAppender httpHeadersAppender() {
        return new HttpHeadersAppender();
    }

    @Bean
    public HttpBodyWriter httpBodyWriter(TaxiiStatusDao taxiiStatusDao) throws Exception {
        return new HttpBodyWriter(taxiiStatusDao);
    }

    @Bean
    public RequestFactory requestFactory() throws Exception {
        return new RequestFactory(
                taxiiServiceSettings.getPollEndpoint(),
                httpRequestFactory(),
                httpHeadersAppender(),
                httpBodyWriter(taxiiStatusDao));
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new BasicAuthHttpRequestFactory(
                httpClient(), taxiiServiceSettings, proxySettings, credentialsProvider());
        factory.setConnectTimeout(300000); //5min
        factory.setConnectionRequestTimeout(300000); //5min
        factory.setReadTimeout(300000); //5min
        return factory;
    }

}
