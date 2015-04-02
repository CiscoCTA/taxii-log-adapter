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
import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.PersistentObjectDelegate;
import org.dellroad.stuff.pobj.SpringDelegate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.threeten.bp.Clock;

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
@EnableConfigurationProperties
@EnableMBeanExport(registration=RegistrationPolicy.FAIL_ON_EXISTING)
@Import(HttpClientConfiguration.class)
public class AdapterConfiguration {

    @Autowired
    private RequestFactory requestFactory;

    @Bean
    public TaxiiServiceSettings taxiiServiceSettings() {
        return new TaxiiServiceSettings();
    }

    @Bean
    public TransformSettings transformSettings() {
        return new TransformSettings();
    }

    @Bean
    public Runnable adapterTask() throws Exception {
        return new AdapterTask(
            requestFactory,
            responseHandler(),
            taxiiServiceSettings().getFeeds(),
            statistics());
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
                taxiiStatusDao(),
                taxiiPollResponseReaderFactory(),
                datatypeFactory(),
                clock());
    }

    @Bean
    public TaxiiPollResponseReaderFactory taxiiPollResponseReaderFactory() throws Exception {
        return new TaxiiPollResponseReaderFactory(inputFactory(), datatypeFactory());
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public XMLInputFactory inputFactory() {
        return XMLInputFactory.newFactory();
    }

    @Bean
    public Templates templates() throws Exception {
        return transformerFactory().newTemplates(
            new StreamSource(
                transformSettings().getStylesheet()));
    }

    @Bean
    public TransformerFactory transformerFactory() throws TransformerFactoryConfigurationError {
        return TransformerFactory.newInstance();
    }

    @Bean
    public TaxiiStatusDao taxiiStatusDao() {
        return new TaxiiStatusDao(taxiiStatusPersistent());
    }

    @Bean
    public PersistentObject<TaxiiStatus> taxiiStatusPersistent() {
        PersistentObject<TaxiiStatus> persistentObject = new PersistentObject<>(
                taxiiStatusPersistentDelegate(),
                taxiiServiceSettings().getStatusFile());
        persistentObject.setAllowEmptyStart(true);
        persistentObject.start();
        return persistentObject;
    }

    @Bean
    public PersistentObjectDelegate<TaxiiStatus> taxiiStatusPersistentDelegate() {
        SpringDelegate<TaxiiStatus> delegate = new SpringDelegate<>();
        delegate.setMarshaller(taxiiStatusMarshaller());
        delegate.setUnmarshaller(taxiiStatusMarshaller());
        return delegate;
    }

    @Bean
    public Jaxb2Marshaller taxiiStatusMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(TaxiiStatus.class);
        jaxb2Marshaller.setMarshallerProperties(ImmutableMap.of(
                javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true));
        return jaxb2Marshaller;
    }

    @Bean
    public DatatypeFactory datatypeFactory() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance();
    }

    @Bean
    public AdapterStatistics statistics() {
        return new AdapterStatistics();
    }

}
