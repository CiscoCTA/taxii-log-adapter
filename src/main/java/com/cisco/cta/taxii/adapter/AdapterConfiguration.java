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

import com.cisco.cta.taxii.adapter.httpclient.HttpClientConfiguration;
import com.cisco.cta.taxii.adapter.persistence.PersistenceConfiguration;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import com.cisco.cta.taxii.adapter.settings.TransformSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;
import java.io.Writer;
import java.time.Clock;

/**
 * Spring configuration providing factory methods for core beans.
 */
@Configuration
@EnableMBeanExport(registration=RegistrationPolicy.FAIL_ON_EXISTING)
@Import({HttpClientConfiguration.class, SettingsConfiguration.class, PersistenceConfiguration.class})
public class AdapterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterConfiguration.class);

    @Autowired
    private TransformSettings transformSettings;

    @Bean
    public Runnable adapterTask(RequestFactory requestFactory, TaxiiServiceSettings taxiiServiceSettings, TaxiiStatusDao taxiiStatusDao) throws Exception {
        return new AdapterTask(
            requestFactory,
            responseTransformer(),
            taxiiServiceSettings,
            statistics(),
            taxiiStatusDao);
    }

    @Bean
    public Writer logWriter() {
        return new Slf4JWriter(
                LoggerFactory.getLogger("output"),
                statistics());
    }

    @Bean
    public Log outputLog() {
        return LogFactory.getLog("output");
    }

    @Bean
    public ResponseTransformer responseTransformer() throws Exception {
        return new ResponseTransformer(
                templates(),
                logWriter(),
                taxiiPollResponseReaderFactory());
    }

    @Bean
    public TaxiiPollResponseReaderFactory taxiiPollResponseReaderFactory() throws Exception {
        return new TaxiiPollResponseReaderFactory(inputFactory(), datatypeFactory());
    }

    @Bean
    public XMLInputFactory inputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
        return xmlInputFactory;
    }

    @Bean
    public Templates templates() throws Exception {
        return transformerFactory().newTemplates(
                new StreamSource(
                        transformSettings.getStylesheet()));
    }

    @Bean
    public TransformerFactory transformerFactory() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("ACCESS_EXTERNAL_DTD configuration is not supported: " + e.getMessage());
        }
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("ACCESS_EXTERNAL_STYLESHEET configuration is not supported: " + e.getMessage());
        }
        return transformerFactory;
    }

    @Bean
    public AdapterStatistics statistics() {
        return new AdapterStatistics();
    }

    @Bean
    public DatatypeFactory datatypeFactory() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
