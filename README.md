Please see the project homepage https://github.com/CiscoCTA/taxii-log-adapter/wiki

#### RELEASE NOTES

##### Version 2.0.0

* Due to migration to Spring Boot 2.0 we renamed configuration property `taxiiService` to `taxii-service`
  because camel case configuration properties are no longer supported.

* Slash character `/` in JSON output is now escaped by default and is returned as `\/` according to changes in Saxon-HE 9.7.0-8.
  When you'd like to keep backward compatibility you need to modify stylesheet and use e.g. character maps as shown in
  following example:
  ```
    <xsl:variable name="character-map" as="map(xs:string, xs:string)">
        <xsl:map>
            <xsl:map-entry key="'/'" select="'/'"/>
        </xsl:map>
    </xsl:variable>

    <xsl:variable name="serialize-options" as="map(xs:string, xs:anyAtomicType)">
        <xsl:map>
            <xsl:map-entry key="'method'" select="'json'"/>
            <xsl:map-entry key="'use-character-maps'" select="$character-map"/>
        </xsl:map>
    </xsl:variable>

    <xsl:value-of select="f:replace-doubles-ints(fn:serialize(fn:parse-json(fn:xml-to-json($xml)), $serialize-options))"/>
  ```
  You need to add also XML namespace support `xmlns:xs="http://www.w3.org/2001/XMLSchema"` if it's not already in stylesheet.
