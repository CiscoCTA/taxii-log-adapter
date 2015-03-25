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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

class StatusFileContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final File STATUS_FILE = new File("target/taxii-status.xml");

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        FileUtils.deleteQuietly(STATUS_FILE);
    }

}
