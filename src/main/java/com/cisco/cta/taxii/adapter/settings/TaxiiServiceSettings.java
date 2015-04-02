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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds taxiiService configuration parameters.
 */
@ConfigurationProperties(prefix="taxiiService")
@Data
public class TaxiiServiceSettings {

    @NotNull
    private URL pollEndpoint;

    @NotNull
    private String username;

    @NotNull
    private String password;

    @Size(min = 1)
    private final List<String> feeds = new ArrayList<>();

    @NotNull
    private File statusFile;

}
