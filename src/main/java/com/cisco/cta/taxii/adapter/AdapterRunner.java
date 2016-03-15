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

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.cisco.cta.taxii.adapter.error.ChainHandler;
import com.cisco.cta.taxii.adapter.error.Handler;
import com.cisco.cta.taxii.adapter.smoketest.SmokeTestConfiguration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Adapter main class.
 */
public class AdapterRunner {

    static ConfigurableApplicationContext ctx;
    private static final Handler<Throwable> errHandler = new ChainHandler();

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) throws Throwable {
        try {
            ctx = new SpringApplicationBuilder(
                    ScheduleConfiguration.class,
                    RunNowConfiguration.class,
                    SmokeTestConfiguration.class)
                .bannerMode(Mode.OFF)
                .listeners(new ApplicationPidFileWriter())
                .web(false)
                .run(args);
            ctx.start();

        } catch (Throwable err) {
            errHandler.handle(err);
        }
    }

    @SuppressFBWarnings(value="DM_EXIT", justification="Spring BOOT requires use of System.exit")
    public static void exit() {
        System.exit(SpringApplication.exit(ctx));
    }
}
