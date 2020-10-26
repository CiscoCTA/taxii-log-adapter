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
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

public class XslVersionTest {
    
    private Source xsltSource;
    private Source dummyXmlSource;

    @Before
    public void setUp() throws Exception {
        dummyXmlSource = new StreamSource(new StringReader("<dummy/>"));
        xsltSource = new StreamSource(XslVersionTest.class.getResourceAsStream("/xsl-version.xsl"));
    }

    /*
        https://developer.mozilla.org/en-US/docs/Web/XPath/Functions/system-property
        xsl:version - a number giving the version of XSLT implemented by the processor; for XSLT processors implementing
                      the version of XSLT specified by this document
        Quite arguable what this test actually tests
     */
    @Test
    public void xslProcessorVersionIs3() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
        StringWriter versionWriter = new StringWriter();
        Result versionResult = new StreamResult(versionWriter);
        transformer.transform(dummyXmlSource, versionResult);
        assertThat(versionWriter.toString(), is("3.0"));
    }
}
