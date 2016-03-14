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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ConfigTask implements Runnable {

    private final String configDirectory;

    public ConfigTask(String configDirectory) {
        this.configDirectory = configDirectory;
    }

    @Override
    public void run() {
        Path configDir = Paths.get(configDirectory);
        Path[] templates = new Path[]{
                Paths.get(configDirectory,"application.yml"),
                Paths.get(configDirectory,"logback.xml"),
                Paths.get(configDirectory,"stix2stix.xsl"),
                Paths.get(configDirectory,"stix2json.xsl"),
                Paths.get(configDirectory,"stix2cef.xsl")
        };
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                log.info("Creating directory {}", configDir);
            }
            for (Path template : templates) {
                if (!Files.exists(template)) {
                    try (InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("config/template/" + template.getFileName())) {
                        Files.copy(stream, template);
                    }
                }
            }
        } catch (IOException e) {
            log.error("I/O Error", e);
        }
    }
}


