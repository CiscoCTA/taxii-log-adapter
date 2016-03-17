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
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:t="http://taxii.mitre.org/messages/taxii_xml_binding-1.1"
                xmlns:s="http://stix.mitre.org/stix-1"
                xmlns:inc="http://stix.mitre.org/Incident-1"
                xmlns:ttp="http://stix.mitre.org/TTP-1"
                xmlns:ind="http://stix.mitre.org/Indicator-2"
                xmlns:sc="http://stix.mitre.org/common-1"
                xmlns:cc="http://cybox.mitre.org/common-2"
                xmlns:f="local-functions">

    <xsl:output method="text" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>


    <xsl:template match="/t:Poll_Response">
        <xsl:apply-templates select="t:Content_Block/t:Content"/>
    </xsl:template>

    <xsl:template match="/t:Status_Message">
        <!-- Ignore Status_Message -->
    </xsl:template>

    <xsl:template match="s:STIX_Package">
        <xsl:apply-templates select="s:Incidents">
            <xsl:with-param name="customer" select="s:STIX_Header/s:Information_Source/sc:Identity/@id"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="s:Incident">
        <xsl:param name="customer"/>
        <xsl:apply-templates select="inc:Related_Indicators">
            <xsl:with-param name="customer" select="$customer"/>
            <xsl:with-param name="incidentId" select="@id"/>
            <xsl:with-param name="incidentTitle" select="inc:Title"/>
            <xsl:with-param name="victim" select="inc:Victim/sc:Name"/>
            <xsl:with-param name="risk" select="inc:Intended_Effect[sc:Description='Risk']/sc:Value"/>
            <xsl:with-param name="confidence" select="inc:Confidence/sc:Value"/>
            <xsl:with-param name="tool" select="inc:Information_Source/sc:Tools/cc:Tool/@idref"/>
            <xsl:with-param name="url" select="@URL"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="sc:Indicator">
        <xsl:param name="customer"/>
        <xsl:param name="incidentId"/>
        <xsl:param name="incidentTitle"/>
        <xsl:param name="victim"/>
        <xsl:param name="risk"/>
        <xsl:param name="confidence"/>
        <xsl:param name="tool"/>
        <xsl:param name="url"/>
        <xsl:apply-templates select="ind:Observable">
            <xsl:with-param name="customer" select="$customer"/>
            <xsl:with-param name="incidentId" select="$incidentId"/>
            <xsl:with-param name="incidentTitle" select="$incidentTitle"/>
            <xsl:with-param name="victim" select="$victim"/>
            <xsl:with-param name="risk" select="$risk"/>
            <xsl:with-param name="confidence" select="$confidence"/>
            <xsl:with-param name="tool" select="$tool"/>
            <xsl:with-param name="url" select="$url"/>
            <xsl:with-param name="indicatorId" select="@id"/>
            <xsl:with-param name="activity" select="ind:Indicated_TTP/sc:TTP/ttp:Title"/>
            <xsl:with-param name="campaign" select="ind:Related_Campaigns/ind:Related_Campaign/sc:Campaign/@idref"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="cc:Custom_Properties">

        <xsl:param name="customer"/>
        <xsl:param name="incidentId"/>
        <xsl:param name="incidentTitle"/>
        <xsl:param name="victim"/>
        <xsl:param name="risk"/>
        <xsl:param name="confidence"/>
        <xsl:param name="tool"/>
        <xsl:param name="url"/>
        <xsl:param name="indicatorId"/>
        <xsl:param name="activity"/>
        <xsl:param name="campaign"/>
        
        <xsl:variable name="xml">
            <xsl:call-template name="webFlowXml">
                <xsl:with-param name="customer" select="$customer"/>
                <xsl:with-param name="incidentId" select="$incidentId"/>
                <xsl:with-param name="incidentTitle" select="$incidentTitle"/>
                <xsl:with-param name="victim" select="$victim"/>
                <xsl:with-param name="risk" select="$risk"/>
                <xsl:with-param name="confidence" select="$confidence"/>
                <xsl:with-param name="tool" select="$tool"/>
                <xsl:with-param name="url" select="$url"/>
                <xsl:with-param name="indicatorId" select="$indicatorId"/>
                <xsl:with-param name="activity" select="$activity"/>
                <xsl:with-param name="campaign" select="$campaign"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:value-of select="f:replace-doubles-ints(fn:xml-to-json($xml))"/>
        <xsl:text>&#10;</xsl:text><!-- EOL -->

    </xsl:template>


    <xsl:template name="webFlowXml">

        <xsl:param name="customer"/>
        <xsl:param name="incidentId"/>
        <xsl:param name="incidentTitle"/>
        <xsl:param name="victim"/>
        <xsl:param name="risk"/>
        <xsl:param name="confidence"/>
        <xsl:param name="tool"/>
        <xsl:param name="url"/>
        <xsl:param name="indicatorId"/>
        <xsl:param name="activity"/>
        <xsl:param name="campaign"/>

        <fn:map>

            <xsl:for-each select="cc:Property">
                <xsl:choose>
                    <xsl:when test="string(number(.)) = 'NaN'">
                        <fn:string>
                            <xsl:attribute name="key" select="@name"/>
                            <xsl:value-of select="."/>
                        </fn:string>
                    </xsl:when>
                    <xsl:otherwise>
                        <fn:number>
                            <xsl:attribute name="key" select="@name"/>
                            <xsl:value-of select="."/>
                        </fn:number>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <fn:string key="customer"><xsl:value-of select="$customer"/></fn:string>
            <fn:string key="incidentId"><xsl:value-of select="$incidentId"/></fn:string>
            <fn:string key="incidentTitle"><xsl:value-of select="$incidentTitle"/></fn:string>
            <fn:string key="victim"><xsl:value-of select="$victim"/></fn:string>
            <fn:string key="risk"><xsl:value-of select="$risk"/></fn:string>
            <fn:string key="confidence"><xsl:value-of select="$confidence"/></fn:string>
            <fn:string key="tool"><xsl:value-of select="$tool"/></fn:string>
            <fn:string key="indicatorId"><xsl:value-of select="$indicatorId"/></fn:string>
            <fn:string key="activity"><xsl:value-of select="$activity"/></fn:string>
            <fn:string key="campaign"><xsl:value-of select="$campaign"/></fn:string>
            <fn:string key="incidentUrl"><xsl:value-of select="$url"/></fn:string>

        </fn:map>
    </xsl:template>


    <xsl:function name="f:replace-doubles-ints">
        <xsl:param name="json"/>

        <xsl:analyze-string select="$json" regex="&quot;(\w*)&quot;:((\d)\.(\d+)[eE](\d+))" flags="x">
            <xsl:matching-substring>
                <xsl:text>"</xsl:text>
                <xsl:value-of select="fn:regex-group(1)"/>
                <xsl:text>"</xsl:text>
                <xsl:text>:</xsl:text>
                <xsl:value-of select="fn:format-number(fn:number(fn:regex-group(2)), '#')"/>
            </xsl:matching-substring>

            <xsl:non-matching-substring>
                <xsl:value-of select="."/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>

    </xsl:function>

</xsl:stylesheet>