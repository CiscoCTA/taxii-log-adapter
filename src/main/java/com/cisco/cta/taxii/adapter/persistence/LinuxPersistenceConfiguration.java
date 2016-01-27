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

import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.PersistentObjectDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;

/**
 * Unix/Linux specific part of the {@link PersistenceConfiguration}.
 */
@Configuration
@Import(SettingsConfiguration.class)
public class LinuxPersistenceConfiguration {

    @Autowired
    TaxiiServiceSettings taxiiServiceSettings;
    
    @Autowired
    Jaxb2Marshaller taxiiStatusMarshaller;
    
    @Bean
    public TaxiiStatusFileHandler taxiiStatusFileHandler() {
        return new TransactionalFileHandler(taxiiStatusPersistent());
    }

    @Bean
    public PersistentObject<TaxiiStatus> taxiiStatusPersistent() {
        return new PersistenceObjectFactory(taxiiServiceSettings.getStatusFile(), taxiiStatusPersistentDelegate()).build();
    }

    @Bean
    public PersistentObjectDelegate<TaxiiStatus> taxiiStatusPersistentDelegate() {
        return new TaxiiStatusDelegate(taxiiStatusMarshaller);
    }

}
