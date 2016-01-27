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

import javax.annotation.PreDestroy;

import org.dellroad.stuff.pobj.PersistentObject;

import lombok.RequiredArgsConstructor;

/**
 * System crash during the {@link #save(TaxiiStatus)} will not corrupt the file.
 */
@RequiredArgsConstructor
public class TransactionalFileHandler implements TaxiiStatusFileHandler {

    private final PersistentObject<TaxiiStatus> pobj;

    @Override
    public TaxiiStatus load() {
        return pobj.getRoot();
    }

    @Override
    public void save(TaxiiStatus status) {
        pobj.setRoot(status);
    }

    @PreDestroy
    public void close() {
        pobj.stop();
    }
}
