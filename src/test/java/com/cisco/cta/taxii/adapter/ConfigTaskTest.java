package com.cisco.cta.taxii.adapter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ConfigTaskTest {
    @Test
    public void createsConfigDir() throws Exception {
        Path configDir = Paths.get("target/config");
        FileUtils.deleteDirectory(configDir.toFile());
        ConfigTask cfTask= new ConfigTask(configDir.toString());
        cfTask.run();

        assertTrue(Files.exists(configDir));
        assertTrue(Files.exists(Paths.get(configDir.toString(),"application.yml")));
        assertTrue(Files.exists(Paths.get(configDir.toString(),"logback.xml")));
        assertTrue(Files.exists(Paths.get(configDir.toString(),"stix2cef.xsl")));
        assertTrue(Files.exists(Paths.get(configDir.toString(),"stix2json.xsl")));
        assertTrue(Files.exists(Paths.get(configDir.toString(),"stix2stix.xsl")));

    }
}