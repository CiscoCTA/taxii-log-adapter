package com.cisco.cta.taxii.adapter.persistence;

import com.cisco.cta.taxii.adapter.settings.TaxiiServiceSettings;
import org.dellroad.stuff.pobj.PersistentObject;
import org.dellroad.stuff.pobj.PersistentObjectDelegate;

import java.io.File;

public class PersistenceObjectFactory {

    private TaxiiServiceSettings taxiiServiceSettings;
    private PersistentObjectDelegate<TaxiiStatus> delegate;

    public PersistenceObjectFactory(TaxiiServiceSettings taxiiServiceSettings, PersistentObjectDelegate<TaxiiStatus> delegate) {
        this.taxiiServiceSettings = taxiiServiceSettings;
        this.delegate = delegate;
    }

    public PersistentObject<TaxiiStatus> build() {
        File statusFile = taxiiServiceSettings.getStatusFile();
        PersistentObject<TaxiiStatus> persistentObject = new PersistentObject<>(delegate, statusFile);
        persistentObject.setAllowEmptyStart(true);
        persistentObject.start();
        if (!statusFile.exists()) {
            throw new RuntimeException("Cannot create status file: " + statusFile.getPath() + ". Please check that the given path is correct and writable.");
        }
        return persistentObject;
    }

}
