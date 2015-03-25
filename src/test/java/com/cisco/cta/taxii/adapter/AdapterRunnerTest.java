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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;

import com.cisco.cta.taxii.adapter.AdapterConfiguration;
import com.cisco.cta.taxii.adapter.AdapterRunner;


public class AdapterRunnerTest {
    
    private static final File PID_FILE = new File("application.pid");

    private static final int RUNNING = -1_000_000;

    private int exitCode;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteQuietly(PID_FILE);
        String[] args = new String[] {"--foo.param=argument-value"};
        AdapterRunner.main(args);
        exitCode = RUNNING;
    }

    @After
    public void tearDown() {
        if (exitCode == RUNNING) {
            SpringApplication.exit(AdapterRunner.ctx);
        }
    }

    @Test
    public void runApplication() throws Exception {
        assertTrue("PID file created", PID_FILE.isFile());
        assertThat(AdapterRunner.ctx.getBean(AdapterConfiguration.class), notNullValue());
        assertTrue(AdapterRunner.ctx.isRunning());
        Environment env = AdapterRunner.ctx.getBean(Environment.class);
        assertThat(env.getProperty("foo.param"), is("argument-value"));
        exitCode = SpringApplication.exit(AdapterRunner.ctx);
        assertThat(exitCode, is(0));
    }
}
