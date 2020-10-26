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
package com.cisco.cta.taxii.adapter;

public class TaxiiStatusException extends Exception {
    private final String statusType;
    private final String statusMessage;

    public TaxiiStatusException(String statusType, String statusMessage) {
        super("Taxii status type: " + statusType + ", status message: " + statusMessage);
        this.statusType = statusType;
        this.statusMessage = statusMessage;
    }

    public String getStatusType() {
        return statusType;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
