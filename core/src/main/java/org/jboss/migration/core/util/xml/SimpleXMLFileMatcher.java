/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.core.util.xml;

import org.jboss.migration.core.ServerMigrationFailureException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * A simple extendable implementation of a {@link XMLFileMatcher}, which by default will match any file with a name ending with ".xml".
 * @author emmartins
 */
public abstract class SimpleXMLFileMatcher implements XMLFileMatcher {

    @Override
    public boolean matches(Path path) throws ServerMigrationFailureException {
        boolean match = false;
        final String fileName = path.getFileName().toString();
        if (fileNameMatches(fileName)) {
            try (final InputStream inputStream = Files.newInputStream(path)) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                reader.require(START_DOCUMENT, null, null);
                while (reader.hasNext()) {
                    if (reader.next() == START_ELEMENT) {
                        if (documentElementLocalNameMatches(reader.getLocalName()) && documentNamespaceURIMatches(reader.getNamespaceURI())) {
                            match = true;
                        }
                        break;
                    }
                }
            } catch (XMLStreamException | IOException e) {
                throw new ServerMigrationFailureException("failed to parse xml file "+path, e);
            }
        }
        return match;
    }

    /**
     * Indicates if the XML file name is a match.
     * @param fileName the XML file name
     * @return true if the file name is a match, false otherwise
     */
    protected boolean fileNameMatches(String fileName) {
        return fileName.endsWith(".xml");
    }

    /**
     * Indicates if the XML file document element's local name is a match.
     * @param localName the XML file document element's local name
     * @return true if the document element's local name is a match, false otherwise
     */
    protected boolean documentElementLocalNameMatches(String localName) {
        return true;
    }

    /**
     * Indicates if the XML file document element's namespace URI is a match.
     * @param namespaceURI the XML file document element's namespace URI
     * @return true if the document element's namespace URI is a match, false otherwise
     */
    protected boolean documentNamespaceURIMatches(String namespaceURI) {
        return true;
    }
}
