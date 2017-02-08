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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.StartElement;

/**
 * A XML file content filter.
 * @author emmartins
 */
public interface XMLFileFilter {

    /**
     * Filters an XML element.
     * @param startElement the element to filter
     * @param xmlEventReader the source XML file reader
     * @param xmlEventWriter the target xml file writer
     * @return the {@link Result} of the element filtering
     * @throws ServerMigrationFailureException if there was a failure filtering the element
     */
    Result filter(StartElement startElement, XMLEventReader xmlEventReader, XMLEventWriter xmlEventWriter) throws ServerMigrationFailureException;

    /**
     * The element filtering result.
     */
    enum Result {
        /**
         * the xml element should be kept
         */
        KEEP,
        /**
         * the filter is not applicable to the element
         */
        NOT_APPLICABLE,
        /**
         * the xml element should be removed
         */
        REMOVE
    }
}
