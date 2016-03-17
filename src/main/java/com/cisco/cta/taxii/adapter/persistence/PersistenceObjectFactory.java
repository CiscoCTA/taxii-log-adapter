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

import lombok.RequiredArgsConstructor;

import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.PersistentObjectDelegate;

import java.io.File;


@RequiredArgsConstructor
public class PersistenceObjectFactory {

    private final File statusFile;
    private final PersistentObjectDelegate<TaxiiStatus> delegate;

    public PersistentObject<TaxiiStatus> build() {
        PersistentObject<TaxiiStatus> persistentObject = new PersistentObject<>(delegate, statusFile);
        persistentObject.setAllowEmptyStart(true);
        persistentObject.start();
        if (!statusFile.exists()) {
            throw new RuntimeException("Cannot create status file: " + statusFile.getPath() + ". Please check that the given path is correct and writable.");
        }
        return persistentObject;
    }

}
