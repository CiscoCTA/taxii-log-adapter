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
package com.cisco.cta.taxii.adapter.error;

import java.io.PrintStream;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CauseHandler implements Handler<Throwable> {

    private final PrintStream err;


    @Override
    public void handle(Throwable t) {
        if (t.getMessage() != null) {
            err.println(t);
        }
        if (t.getCause() != null) {
            handle(t.getCause());
        }
    }

}
