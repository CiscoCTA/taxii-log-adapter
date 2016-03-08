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

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.cisco.cta.taxii.adapter.settings.BindExceptionHandler;
import com.cisco.cta.taxii.adapter.smoketest.SmokeTestConfiguration;


/**
 * Adapter main class.
 */
public class AdapterRunner {

    static ConfigurableApplicationContext ctx;
    private static BindExceptionHandler bindExceptionHandler = new BindExceptionHandler(System.err);

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        try {
            ctx = new SpringApplicationBuilder(
                    AdapterConfiguration.class,
                    ScheduleConfiguration.class,
                    RunNowConfiguration.class,
                    SmokeTestConfiguration.class)
                .showBanner(false)
                .listeners(new ApplicationPidFileWriter())
                .run(args);
            ctx.start();

        } catch (NestedRuntimeException e) {
            try {
                throw e.getMostSpecificCause();
            } catch (BindException bindRootCause) {
                bindExceptionHandler.handle(bindRootCause);
            } catch (Throwable unknownRootCause) {
                System.err.println("CRITICAL UNKNOWN ERROR WHILE INITIALIZING");
                throw e;
            }

        } catch (RuntimeException e) {
            System.err.println("CRITICAL UNKNOWN ERROR WHILE INITIALIZING");
            throw e;
        }
    }

}
