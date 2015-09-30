<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:t="http://taxii.mitre.org/messages/taxii_xml_binding-1.1">

    <xsl:template match="/">
        <xsl:copy-of select="/t:Poll_Response/t:Content_Block/t:Content/node()"/>
    </xsl:template>

</xsl:stylesheet>
