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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.mockito.ArgumentMatcher;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;


public class IsEventContaining implements ArgumentMatcher<ILoggingEvent> {
    
    private final String substring;

    private IsEventContaining(String substring) {
        this.substring = substring;
    }

    public static void verifyLog(Appender<ILoggingEvent> mockAppender, final String substring) {
        verify(mockAppender, atLeastOnce())
                .doAppend(argThat(isEventContaining(substring)));
    }

    public static IsEventContaining isEventContaining(String substring) {
        return new IsEventContaining(substring);
    }

    @Override
    public boolean matches(ILoggingEvent iLoggingEvent) {
        return iLoggingEvent.getMessage().contains(substring);
    }
}
