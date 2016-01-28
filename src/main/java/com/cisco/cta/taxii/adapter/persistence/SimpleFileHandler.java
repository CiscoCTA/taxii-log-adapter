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
import java.io.FileNotFoundException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * System crash during the {@link #save(TaxiiStatus)} may corrupt the file.
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleFileHandler implements TaxiiStatusFileHandler {

    private final File file;
    private final Jaxb2Marshaller marshaller;

    @Override
    public TaxiiStatus load() {
        try {
            Source source = new StreamSource(file);
            return (TaxiiStatus) marshaller.unmarshal(source);
        } catch (UnmarshallingFailureException e) {
            if (e.contains(FileNotFoundException.class)) {
                log.info("File {} doesn't exist yet", file);
                return new TaxiiStatus();
            } else {
                throw e;
            }
        }
    }

    @Override
    public void save(TaxiiStatus status) {
        Result result = new StreamResult(file);
        marshaller.marshal(status, result);
    }

}
