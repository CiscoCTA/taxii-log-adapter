<!--=====================================================================================
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
=====================================================================================-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslt="http://xml.apache.org/xslt"
                xmlns:t="http://taxii.mitre.org/messages/taxii_xml_binding-1.1">

    <xsl:output method="xml" indent="yes" xslt:indent-amount="4" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:copy-of select="/t:Poll_Response/t:Content_Block/t:Content/node()"/>
    </xsl:template>

</xsl:stylesheet>
