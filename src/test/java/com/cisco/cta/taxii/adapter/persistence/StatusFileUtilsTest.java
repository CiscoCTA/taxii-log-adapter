package com.cisco.cta.taxii.adapter.persistence;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class StatusFileUtilsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test(expected = RuntimeException.class)
    public void statusFileCannotBeCreated() {
        StatusFileUtils.testIfFileCanBeCreated(new File("/nonexistent/path"));
    }

    @Test
    public void statusFileCanBeCreated() throws IOException {
        File file = new File(tmpFolder.getRoot().getAbsolutePath() + "/taxii-status-tmp-" + System.currentTimeMillis() + ".xml");
        StatusFileUtils.testIfFileCanBeCreated(file);
    }

}