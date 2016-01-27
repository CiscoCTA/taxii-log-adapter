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
