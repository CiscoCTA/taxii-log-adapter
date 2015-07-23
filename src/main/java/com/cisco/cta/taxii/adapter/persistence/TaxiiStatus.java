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

package com.cisco.cta.taxii.adapter.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.Data;

/**
 * JAXB annotated class holding consumption status of the TAXII service feeds.
 * @see TaxiiStatusDao
 */
@Data
@XmlRootElement(name="taxii-status")
@XmlAccessorType(XmlAccessType.NONE)
public class TaxiiStatus {

    @XmlElement
    private final List<Feed> feed = new ArrayList<>();
    
    
    /**
     * JAXB annotated class holding consumption status of a single TAXII feed.
     * @see TaxiiStatusDao
     */
    @Data
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Feed {
        
        @XmlAttribute
        private String name;

        @XmlAttribute
        private XMLGregorianCalendar lastUpdate;

        @XmlAttribute
        private Boolean more;

        @XmlAttribute
        private String resultId;

        @XmlAttribute
        private Integer resultPartNumber;

    }
}
