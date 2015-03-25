<?xml version="1.0"?>
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

  <xsl:template match="/">
        <xsl:value-of select="system-property('xsl:version')" />
  </xsl:template>

</xsl:stylesheet>