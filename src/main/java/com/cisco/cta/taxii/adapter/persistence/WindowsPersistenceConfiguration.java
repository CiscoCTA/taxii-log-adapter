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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.cisco.cta.taxii.adapter.WindowsCondition;
import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * Unix/Linux specific part of the {@link PersistenceConfiguration}.
 */
@Configuration
@Conditional(WindowsCondition.class)
@Import({SettingsConfiguration.class, CommonPersistenceConfiguration.class})
public class WindowsPersistenceConfiguration {

    @Autowired
    private TaxiiServiceSettings taxiiServiceSettings;
    
    @Autowired
    private Jaxb2Marshaller taxiiStatusMarshaller;
    
    @Bean
    public TaxiiStatusFileHandler taxiiStatusFileHandler() {
        return new SimpleFileHandler(taxiiServiceSettings.getStatusFile(), taxiiStatusMarshaller);
    }
}
