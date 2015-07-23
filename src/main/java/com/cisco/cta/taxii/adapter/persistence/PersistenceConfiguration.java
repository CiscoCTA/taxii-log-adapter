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

package com.cisco.cta.taxii.adapter.persistence;

import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import com.google.common.collect.ImmutableMap;
import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.PersistentObjectDelegate;
import org.dellroad.stuff.pobj.SpringDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.threeten.bp.Clock;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * Spring configuration TAXII status persistence support.
 */
@Configuration
@Import(SettingsConfiguration.class)
public class PersistenceConfiguration {

    @Autowired
    private TaxiiServiceSettings taxiiServiceSettings;

    @Bean
    public TaxiiStatusDao taxiiStatusDao() throws DatatypeConfigurationException {
        return new TaxiiStatusDao(taxiiStatusPersistent(),
                datatypeFactory(),
                clock());
    }

    @Bean
    public PersistentObject<TaxiiStatus> taxiiStatusPersistent() {
        PersistentObject<TaxiiStatus> persistentObject = new PersistentObject<>(
                taxiiStatusPersistentDelegate(),
                taxiiServiceSettings.getStatusFile());
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
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public DatatypeFactory datatypeFactory() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance();
    }

}
