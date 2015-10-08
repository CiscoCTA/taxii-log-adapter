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
