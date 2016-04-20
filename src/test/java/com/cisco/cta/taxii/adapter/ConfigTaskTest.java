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
        assertTrue(Files.exists(Paths.get(configDir.toString(),"taxii-response.xsl")));

    }
}