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

import java.net.MalformedURLException;
import java.net.URL;

import com.cisco.cta.taxii.adapter.TaxiiServiceSettings;

public class TaxiiServiceSettingsFactory {

    public static TaxiiServiceSettings createDefaults() {
        try {
            TaxiiServiceSettings connSettings = new TaxiiServiceSettings();
            connSettings.setPollEndpoint(new URL("http://localhost:8080/service"));
            connSettings.setUsername("user");
            connSettings.setPassword("pass");
            return connSettings;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
