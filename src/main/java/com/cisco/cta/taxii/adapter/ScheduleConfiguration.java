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

import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.cisco.cta.taxii.adapter.settings.ScheduleSettings;

/**
 * Spring configuration providing factory methods for scheduling beans.
 */
@Configuration
@Profile("schedule")
public class ScheduleConfiguration {

    @Autowired
    private AdapterConfiguration adapterConfiguration;

    @Autowired
    private ScheduleSettings scheduleSettings;
    
    @Bean
    public TaskScheduler taskScheduler() throws Exception {
        return new ThreadPoolTaskScheduler();
    }
    
    @Bean
    public ScheduledFuture<?> scheduleAdapterTask() throws Exception {
        return taskScheduler().schedule(
            adapterConfiguration.adapterTask(),
            new CronTrigger(scheduleSettings.getCron()));
    }
    
}
