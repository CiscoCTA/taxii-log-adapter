/*
   Copyright 2016 Cisco Systems

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
