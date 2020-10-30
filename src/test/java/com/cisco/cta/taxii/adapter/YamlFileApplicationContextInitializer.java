package com.cisco.cta.taxii.adapter;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class YamlFileApplicationContextInitializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Resource resource = applicationContext.getResource("classpath:/config/application.yml");
            YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
            List<PropertySource<?>> yamlTestPropertiesList = sourceLoader.load("application.yml", resource);
            yamlTestPropertiesList.forEach(yamlTestProperties -> applicationContext.getEnvironment().getPropertySources().addLast(yamlTestProperties));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
