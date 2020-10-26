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
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.springframework.validation.annotation.Validated;

/**
 * Holds taxiiService configuration parameters.
 */
@ConfigurationProperties(prefix="taxii-service")
@Data
@Validated
public class TaxiiServiceSettings {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @NotNull
    private URL pollEndpoint;

    @NotNull
    private String username;

    @NotNull
    private String password;

    private List<String> feeds;

    private File feedNamesFile;

    private File statusFile = new File("taxii-status.xml");

    @PostConstruct
    public void loadFeedNames() throws IOException {
        Preconditions.checkState(
            feeds != null ^ feedNamesFile != null,
            "taxii-service.feeds or taxii-service.feedNamesFile must be set");
        if (feedNamesFile != null) {
            feeds = Files.readLines(feedNamesFile, UTF8);
        }
    }
}
