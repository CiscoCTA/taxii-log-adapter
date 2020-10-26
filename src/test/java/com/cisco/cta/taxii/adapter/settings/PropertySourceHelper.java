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
package com.cisco.cta.taxii.adapter.settings;

import org.springframework.mock.env.MockPropertySource;


class PropertySourceHelper {

    public static MockPropertySource validProperties() {
        return new MockPropertySource()
            .withProperty("taxii-service.pollEndpoint", "http://taxii")
            .withProperty("taxii-service.username", "smith")
            .withProperty("taxii-service.password", "secret")
            .withProperty("taxii-service.feeds[0]", "alpha-feed")
            .withProperty("taxii-service.statusFile", "taxii-status.xml")
            .withProperty("schedule.cron", "* * * * * *")
            .withProperty("transform.stylesheet", "transform.xsl")
            .withProperty("proxy.url", "http://localhost:8001/")
            .withProperty("proxy.authenticationType", "NONE");
    }

    public static MockPropertySource exclude(MockPropertySource all, String excludePrefix) {
        MockPropertySource source = new MockPropertySource();
        for (String key : all.getPropertyNames()) {
            if (! key.startsWith(excludePrefix)) {
                source.setProperty(key, all.getProperty(key));
            }
        }
        return source;
    }

}
