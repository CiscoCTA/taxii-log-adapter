Please see the project homepage https://github.com/CiscoCTA/taxii-log-adapter/wiki

#### RELEASE NOTES

##### Version 2.0.0

* Configuration property `taxiiService` was renamed to `taxii-service`.
  Camel case configuration properties are no longer supported.

* Slash character `/` in JSON output is now escaped by default and it's returned as `\/` according to the changes in Saxon-HE 9.7.0-8.
  If you'd like to keep backward compatibility you can modify the stylesheet and use character maps as shown in the following example:
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
  You need to also add XML namespace support `xmlns:xs="http://www.w3.org/2001/XMLSchema"` if it's not already in the stylesheet.
