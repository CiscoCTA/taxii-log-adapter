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

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import lombok.RequiredArgsConstructor;

/**
 * System crash during the {@link #save(TaxiiStatus)} may corrupt the file.
 */
@RequiredArgsConstructor
public class SimpleFileHandler implements TaxiiStatusFileHandler {

    private final File file;
    private final Jaxb2Marshaller marshaller;

    @Override
    public TaxiiStatus load() {
        Source source = new StreamSource(file);
        return (TaxiiStatus) marshaller.unmarshal(source);
    }

    @Override
    public void save(TaxiiStatus status) {
        Result result = new StreamResult(file);
        marshaller.marshal(status, result);
    }

}
