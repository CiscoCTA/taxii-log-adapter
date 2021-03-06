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

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatusDao;
import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;


/**
 * Spring configuration providing factory methods for run-now beans.
 */
@Configuration
@Profile("now")
@Import(AdapterConfiguration.class)
public class RunNowConfiguration {

    @Bean
    public RunAndExit runAndExit(AdapterConfiguration adapterConfiguration, RequestFactory requestFactory, TaxiiServiceSettings taxiiServiceSettings, TaxiiStatusDao taxiiStatusDao) throws Exception {
        return new RunAndExit(adapterConfiguration.adapterTask(requestFactory, taxiiServiceSettings, taxiiStatusDao));
    }

}
