package com.cisco.cta.taxii.adapter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class SettingsTestConfiguration {

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
