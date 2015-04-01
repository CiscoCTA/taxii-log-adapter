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

import org.springframework.boot.SpringApplication;
import org.springframework.context.Lifecycle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Delegates to a #{@link java.lang.Runnable} instance, then stops the application.
 */
public class RunAndExit implements Lifecycle {

    private final Runnable delegate;
    
    public RunAndExit(Runnable delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressFBWarnings(value="DM_EXIT", justification="Spring BOOT requires use of System.exit")
    public void start() {
        delegate.run();
        System.exit(SpringApplication.exit(AdapterRunner.ctx));
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean isRunning() {
        return false;
    }

}