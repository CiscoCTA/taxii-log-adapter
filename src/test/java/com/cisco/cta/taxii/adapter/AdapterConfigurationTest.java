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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.trans.XPathException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@ContextConfiguration(classes = AdapterConfiguration.class, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource(properties = "spring.config.location=config/template")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdapterConfigurationTest {

    private final URL testFileUri = AdapterConfigurationTest.class.getResource("/feed-names.txt");

    @Autowired
    private AdapterConfiguration adapterCfg;

    @Autowired
    private XMLInputFactory xmlInputFactory;

    @Autowired
    private TransformerFactory transformerFactory;

    @Test
    public void applyConfiguration() {
        assertThat(adapterCfg, notNullValue());
    }

    @Test
    public void jmxStatisticsRegistered() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.cisco.cta.taxii:component=taxii-log-adapter,type=statistics");
        assertTrue(mbs.isRegistered(name));
    }

    @Test
    public void xmlInputFactoryIsResistantToReadDtd() throws Exception {
        File file = Paths.get(testFileUri.toURI()).toFile();

        String fileReadingXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!DOCTYPE foo [\n" +
                "  <!ELEMENT foo ANY >\n" +
                "  <!ENTITY xxe SYSTEM \"file://" + file.getAbsolutePath() + "\" >]>\n" +
                "<foo>&xxe;</foo>";

        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(fileReadingXml.getBytes()));
        assertThat(reader.next(), is(XMLStreamConstants.DTD));
        assertThat(reader.next(), is(XMLStreamConstants.START_ELEMENT));
        assertThat(reader.getLocalName(), is("foo"));
        // in case we have allowed just "isSupportingExternalEntities" here will be XMLStreamConstants.CHARACTERS
        // element but wee disable DTD at all so content variable is not available.
        try {
            reader.next();
            fail();
        } catch (XMLStreamException e) {
            assertThat(e.getMessage(), containsString("The entity \"xxe\" was referenced, but not declared."));
        }
    }

    @Test
    public void transformerFactoryIsResistant() throws Exception {
        File file = Paths.get(testFileUri.toURI()).toFile();

        String fileReadingXsl = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "    <xsl:template match=\"/\">\n" +
                "      <xsl:copy-of select=\"document('" + file.getAbsolutePath() + "')\"/>\n" +
                "    </xsl:template>\n" +
                "  </xsl:stylesheet>";

        String anyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<note>\n" +
                "  <to>Tove</to>\n" +
                "  <from>Jani</from>\n" +
                "  <heading>Reminder</heading>\n" +
                "  <body>Don't forget me this weekend!</body>\n" +
                "</note>";

        Source xslSource = new StreamSource(new ByteArrayInputStream(fileReadingXsl.getBytes(StandardCharsets.UTF_8)));
        Source xmlSource = new StreamSource(new ByteArrayInputStream(anyXml.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result result = new StreamResult(outputStream);

        try {
            transformerFactory.newTemplates(xslSource).newTransformer().transform(xmlSource, result);
            fail();
        } catch (XPathException e) {
            assertThat(e.getMessage(), containsString("feed-names.txt"));
            assertThat(e.getMessage(), containsString("Content is not allowed in prolog"));
        }
    }
}
