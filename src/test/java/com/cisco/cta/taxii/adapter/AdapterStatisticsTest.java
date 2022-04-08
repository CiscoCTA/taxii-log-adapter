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


import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdapterStatisticsTest {

    private AdapterStatistics statistics;

    @Before
    public void setUp() throws Exception {
        statistics = new AdapterStatistics();
    }

    @Test
    public void incrementPolls() throws Exception {
        assertThat(statistics.getPolls(), is(0L));
        statistics.incrementPolls();
        assertThat(statistics.getPolls(), is(1L));
    }

    @Test
    public void incrementLogs() throws Exception {
        assertThat(statistics.getLogs(), is(0L));
        statistics.incrementLogs();
        assertThat(statistics.getLogs(), is(1L));
    }

    @Test
    public void incrementErrors() throws Exception {
        assertThat(statistics.getErrors(), is(0L));
        statistics.incrementErrors();
        assertThat(statistics.getErrors(), is(1L));
    }
}
