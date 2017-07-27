<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" encoding="utf-8" standalone="no" media-type="text/html" />
    <xsl:param name="version"/>
    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz '" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ!'" />
    
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
                <link rel="stylesheet" type="text/css" href="licenses.css"/>
            </head>
            <body>
                <h2>Wildfly Core <xsl:value-of select="substring-before($version, '-')"/></h2>
                <p>The following material has been provided for informational purposes only, and should not be relied upon or construed as a legal opinion or legal advice.</p>
                <!-- Read matching templates -->
                <table>
                    <tr>
                        <th>Package Group</th>
                        <th>Package Artifact</th>
                        <th>Package Version</th>
                        <th>Remote Licenses</th>
                        <th>Local Licenses</th>
                    </tr>
                    <xsl:for-each select="licenseSummary/dependencies/dependency">
                        <xsl:sort select="concat(groupId, '.', artifactId)"/>
                        <tr>
                            <td><xsl:value-of select="groupId"/></td>
                            <td><xsl:value-of select="artifactId"/></td>
                            <td><xsl:value-of select="version"/></td>
                            <td>
                                <xsl:for-each select="licenses/license">
                                    <a href="{./url}"><xsl:value-of select="name"/></a><br/>
                                </xsl:for-each>
                            </td>
                            <td>
                                <xsl:for-each select="licenses/license">
                                    <xsl:variable name="filename">
                                        <xsl:call-template name="remap-local-filename">
                                            <xsl:with-param name="name" select="name" />
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <a href="{$filename}"><xsl:value-of select="$filename"/></a><br/>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="remap-local-filename">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="$name = 'BSD 3-clause &quot;New&quot; or &quot;Revised&quot; License'">
                <xsl:text>bsd 3-clause new or revised license.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'Common Development and Distribution License 1.1'">
                <xsl:text>common development and distribution license 1.1.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'GNU General Public License v2.0 only'">
                <xsl:text>gnu general public license v2.0 only.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'ICU License'">
                <xsl:text>icu license.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'Indiana University Extreme! Lab Software License 1.1.1'">
                <xsl:text>indiana university extreme lab software license 1.1.1.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'Sax Public Domain Notice'">
                <xsl:text>sax public domain notice.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'W3C Document License'">
                <xsl:text>w3c document license.html</xsl:text>
            </xsl:when>
            <xsl:when test="$name = 'W3C Software Notice and Document License (2002-12-31)'">
                <xsl:text>w3c software notice and document license (2002-12-31).html</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat(translate($name, $uppercase, $lowercase), '.txt')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
