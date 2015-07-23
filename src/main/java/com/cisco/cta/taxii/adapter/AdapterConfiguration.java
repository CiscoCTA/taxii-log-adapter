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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;
import java.io.Writer;

/**
 * Spring configuration providing factory methods for core beans.
 */
@Configuration
@EnableMBeanExport(registration=RegistrationPolicy.FAIL_ON_EXISTING)
@Import({HttpClientConfiguration.class, SettingsConfiguration.class, PersistenceConfiguration.class})
public class AdapterConfiguration {

    @Autowired
    private RequestFactory requestFactory;

    @Autowired
    private TaxiiServiceSettings taxiiServiceSettings;

    @Autowired
    private TransformSettings transformSettings;

    @Autowired
    private TaxiiStatusDao taxiiStatusDao;

    @Autowired
    private DatatypeFactory datatypeFactory;

    @Bean
    public Runnable adapterTask() throws Exception {
        return new AdapterTask(
            requestFactory,
            responseHandler(),
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
    public ResponseHandler responseHandler() throws Exception {
        return new ResponseHandler(
                templates(),
                logWriter(),
                taxiiPollResponseReaderFactory());
    }

    @Bean
    public TaxiiPollResponseReaderFactory taxiiPollResponseReaderFactory() throws Exception {
        return new TaxiiPollResponseReaderFactory(inputFactory(), datatypeFactory);
    }

    @Bean
    public XMLInputFactory inputFactory() {
        return XMLInputFactory.newFactory();
    }

    @Bean
    public Templates templates() throws Exception {
        return transformerFactory().newTemplates(
                new StreamSource(
                        transformSettings.getStylesheet()));
    }

    @Bean
    public TransformerFactory transformerFactory() throws TransformerFactoryConfigurationError {
        return TransformerFactory.newInstance();
    }

    @Bean
    public AdapterStatistics statistics() {
        return new AdapterStatistics();
    }

}
