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

package com.cisco.cta.taxii.adapter.settings;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing all settings beans.
 */
@Configuration
@EnableConfigurationProperties
public class SettingsConfiguration {

    @Bean
    public TaxiiServiceSettings taxiiServiceSettings() {
        return new TaxiiServiceSettings();
    }

    @Bean
    public TransformSettings transformSettings() {
        return new TransformSettings();
    }

    @Bean
    public ScheduleSettings scheduleSettings() {
        return new ScheduleSettings();
    }
    
    @Bean
    public ProxySettings proxySettings() {
        return new ProxySettings();
    }

}
