package com.cisco.cta.taxii.adapter.persistence;

import java.io.File;
import java.io.IOException;

public class StatusFileUtils {

    /**
     * Tests if given status file can be created if it doesn't already exist.
     *
     * @param statusFile
     * @throws RuntimeException in case the file can't be created
     */
    public static void testIfFileCanBeCreated(File statusFile) {
        if (!statusFile.exists()) {
            try {
                if (!statusFile.createNewFile()) {
                    throw new RuntimeException("Cannot create status file: " + statusFile.getPath() + ". Please check that the given path is correct.");
                }
            }catch (IOException e) {
                throw new RuntimeException("Cannot create status file: " + statusFile.getPath() + ". Please check that the given path is correct.");

            }finally {
                statusFile.delete();
            }
        }
    }

}
