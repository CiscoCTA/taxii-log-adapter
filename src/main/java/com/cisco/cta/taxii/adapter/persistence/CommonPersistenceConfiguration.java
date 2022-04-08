package com.cisco.cta.taxii.adapter.persistence;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;

@Configuration
public class CommonPersistenceConfiguration {
    @Bean
    public Jaxb2Marshaller taxiiStatusMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(TaxiiStatus.class);
        jaxb2Marshaller.setMarshallerProperties(ImmutableMap.of(
                Marshaller.JAXB_FORMATTED_OUTPUT, true
        ));
        return jaxb2Marshaller;
    }
}
