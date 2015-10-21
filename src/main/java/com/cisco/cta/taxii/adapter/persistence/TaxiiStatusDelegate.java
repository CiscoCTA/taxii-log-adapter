package com.cisco.cta.taxii.adapter.persistence;

import org.dellroad.stuff.pobj.SpringDelegate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public class TaxiiStatusDelegate extends SpringDelegate<TaxiiStatus> {

    public TaxiiStatusDelegate(Jaxb2Marshaller marshaller) {
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    @Override
    public TaxiiStatus getDefaultValue() {
        return new TaxiiStatus();
    }

};
