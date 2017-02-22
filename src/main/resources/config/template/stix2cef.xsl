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
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:t="http://taxii.mitre.org/messages/taxii_xml_binding-1.1"
                xmlns:s="http://stix.mitre.org/stix-1"
                xmlns:inc="http://stix.mitre.org/Incident-1"
                xmlns:ttp="http://stix.mitre.org/TTP-1"
                xmlns:ind="http://stix.mitre.org/Indicator-2"
                xmlns:sc="http://stix.mitre.org/common-1"
                xmlns:cc="http://cybox.mitre.org/common-2"
                xmlns:c="http://cybox.mitre.org/cybox-2">

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
            <xsl:with-param name="confidence" select="inc:Confidence/sc:Value"/>
            <xsl:with-param name="tool" select="inc:Information_Source/sc:Tools/cc:Tool/@idref"/>
            <xsl:with-param name="risk" select="inc:Intended_Effect[sc:Description='Risk']/sc:Value"/>
            <xsl:with-param name="url" select="@URL"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="sc:Indicator">
        <xsl:param name="customer"/>
        <xsl:param name="incidentId"/>
        <xsl:param name="incidentTitle"/>
        <xsl:param name="victim"/>
        <xsl:param name="confidence"/>
        <xsl:param name="tool"/>
        <xsl:param name="risk"/>
        <xsl:param name="url"/>
        <xsl:apply-templates select="ind:Observable">
            <xsl:with-param name="customer" select="$customer"/>
            <xsl:with-param name="incidentId" select="$incidentId"/>
            <xsl:with-param name="incidentTitle" select="$incidentTitle"/>
            <xsl:with-param name="victim" select="$victim"/>
            <xsl:with-param name="confidence" select="$confidence"/>
            <xsl:with-param name="tool" select="$tool"/>
            <xsl:with-param name="risk" select="$risk"/>
            <xsl:with-param name="url" select="$url"/>
            <xsl:with-param name="indicatorId" select="@id"/>
            <xsl:with-param name="activity" select="ind:Indicated_TTP/sc:TTP/ttp:Title"/>
            <xsl:with-param name="campaign" select="ind:Related_Campaigns/ind:Related_Campaign/sc:Campaign/@idref"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="c:Properties">

        <xsl:param name="customer"/>
        <xsl:param name="incidentId"/>
        <xsl:param name="incidentTitle"/>
        <xsl:param name="victim"/>
        <xsl:param name="confidence"/>
        <xsl:param name="tool"/>
        <xsl:param name="risk"/>
        <xsl:param name="url"/>
        <xsl:param name="indicatorId"/>
        <xsl:param name="activity"/>
        <xsl:param name="campaign"/>

        <xsl:if test=".[@custom_name='cta:webflow']">
            <xsl:text>CEF:0|Cisco|Cognitive Threat Analytics|1.0|1|Web Flow|</xsl:text>
        </xsl:if>

        <xsl:if test=".[@custom_name='cta:netflow']">
            <xsl:text>CEF:0|Cisco|Cognitive Threat Analytics|1.0|1|Net Flow|</xsl:text>
        </xsl:if>

        <xsl:value-of select="$risk"/>
        <xsl:text>|</xsl:text>

        <xsl:if test="cc:Custom_Properties/cc:Property[@name='timestamp']">
            <xsl:call-template name="property">
                <xsl:with-param name="key">start</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='timestamp']"/>
            </xsl:call-template>

            <xsl:if test="cc:Custom_Properties/cc:Property[@name='xElapsedTime']">
                <xsl:call-template name="property">
                    <xsl:with-param name="key">end</xsl:with-param>
                    <xsl:with-param name="value" select="
            format-number(
            number(cc:Custom_Properties/cc:Property[@name='timestamp']) +
            number(cc:Custom_Properties/cc:Property[@name='xElapsedTime']),
            '0')"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>

        <xsl:if test="cc:Custom_Properties/cc:Property[@name='startTime']">
            <xsl:call-template name="property">
                <xsl:with-param name="key">start</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='startTime']"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="cc:Custom_Properties/cc:Property[@name='endTime']">
            <xsl:call-template name="property">
                <xsl:with-param name="key">end</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='endTime']"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test=".[@custom_name='cta:webflow']">
            <xsl:call-template name="property">
                <xsl:with-param name="key">outcome</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='scHttpStatus']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">requestClientApplication</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='csUserAgent']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">out</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='csContentBytes']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">in</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='scContentBytes']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">request</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='csUrl']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">src</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='cIP']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">dst</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='sIP']"/>
            </xsl:call-template>

            <xsl:call-template name="user-property">
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='cUsername']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">cn1Label</xsl:with-param>
                <xsl:with-param name="value">Server Reputation</xsl:with-param>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">cn1</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='sReputation']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">cat</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='sCategory']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">fname</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='scFileName']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">oldFileType</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='scContentType']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">fileType</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='xMagicContentType']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">fileHash</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='scMD5']"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test=".[@custom_name='cta:netflow']">

            <xsl:call-template name="property">
                <xsl:with-param name="key">proto</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='networkProtocol']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">src</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='client.ip']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">dst</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='server.ip']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">out</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='client.totalBytes']"/>
            </xsl:call-template>


            <xsl:call-template name="property">
                <xsl:with-param name="key">smac</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='client.macAddress']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">spt</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='client.firstSeenPort']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">dpt</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='server.firstSeenPort']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">dmac</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='server.macAddress']"/>
            </xsl:call-template>

            <xsl:call-template name="property">
                <xsl:with-param name="key">in</xsl:with-param>
                <xsl:with-param name="value" select="cc:Custom_Properties/cc:Property[@name='server.totalBytes']"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:call-template name="property">
            <xsl:with-param name="key">deviceFacility</xsl:with-param>
            <xsl:with-param name="value" select="$customer"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">reason</xsl:with-param>
            <xsl:with-param name="value" select="$incidentId"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">msg</xsl:with-param>
            <xsl:with-param name="value" select="fn:translate($incidentTitle,'|',':')"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs1Label</xsl:with-param>
            <xsl:with-param name="value">Confidence</xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs1</xsl:with-param>
            <xsl:with-param name="value" select="$confidence"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">act</xsl:with-param>
            <xsl:with-param name="value" select="$activity"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs2Label</xsl:with-param>
            <xsl:with-param name="value">Campaign</xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs2</xsl:with-param>
            <xsl:with-param name="value" select="$campaign"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">sourceServiceName</xsl:with-param>
            <xsl:with-param name="value" select="$tool"/>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs3Label</xsl:with-param>
            <xsl:with-param name="value">IncidentURL</xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="property">
            <xsl:with-param name="key">cs3</xsl:with-param>
            <xsl:with-param name="value" select="$url"/>
            <xsl:with-param name="delimiter"/>
        </xsl:call-template>

        <!-- End of line -->
        <xsl:text>
</xsl:text>
    </xsl:template>


    <xsl:template name="property">
        <xsl:param name="key"/>
        <xsl:param name="value"/>
        <xsl:param name="delimiter">true</xsl:param>
        <xsl:value-of select="$key"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="
            fn:replace(
            fn:replace(
            fn:replace($value,
            '\\', '\\\\'),
            '\|', '\\|'),
            '=', '\\=')"/>
        <xsl:if test="$delimiter">
            <xsl:text> </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template name="user-property">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="fn:contains($value,'\')">
                <xsl:call-template name="property">
                    <xsl:with-param name="key">sntdom</xsl:with-param>
                    <xsl:with-param name="value" select="fn:substring-before($value, '\')"/>
                </xsl:call-template>
                <xsl:call-template name="property">
                    <xsl:with-param name="key">suser</xsl:with-param>
                    <xsl:with-param name="value" select="fn:substring-after($value, '\')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="property">
                    <xsl:with-param name="key">suser</xsl:with-param>
                    <xsl:with-param name="value" select="$value"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>