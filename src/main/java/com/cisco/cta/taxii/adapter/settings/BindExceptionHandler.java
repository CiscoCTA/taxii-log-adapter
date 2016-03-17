/*
   Copyright 2016 Cisco Systems

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

import java.io.PrintStream;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.cisco.cta.taxii.adapter.error.Handler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BindExceptionHandler implements Handler<BindException> {

    private final PrintStream err;

    @Override
    public void handle(BindException e) {
        for(ObjectError error : e.getAllErrors()) {
            StringBuilder b = new StringBuilder()
                .append("Error in application.yml")
                .append(", section ").append(error.getObjectName());
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                b.append(", ").append(fieldError.getField())
                .append(" has illegal value ").append(fieldError.getRejectedValue());
            }
            err.println(b.toString());
        }
    }
}
