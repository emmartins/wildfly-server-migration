/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.eap.task.subsystem.transactions;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfiguration;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.SimpleComponentTask;
import org.jboss.migration.core.util.xml.XMLFileFilter;
import org.jboss.migration.core.util.xml.XMLFiles;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * @author emmartins
 */
public class UpdateObjectStorePath<S extends JBossServer<S>> implements ServerConfigurationMigration.XMLConfigurationSubtaskFactory<JBossServerConfiguration<S>> {

    public static final String TASK_NAME = "subsystem.transactions.update-xml-object-store-paths";

    @Override
    public ServerMigrationTask getTask(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath) {
        return new SimpleComponentTask.Builder()
                .name(TASK_NAME)
                .skipPolicy(skipIfDefaultTaskSkipPropertyIsSet())
                .runnable(context -> {
                    context.getLogger().debugf("Searching for transactions subsystem XML configurations with deprecated object store paths, not supported by the target server...");
                    final ServerMigrationTaskResult taskResult = processXMLConfiguration(source, targetConfigurationPath, context);
                    if (taskResult.getStatus() == ServerMigrationTaskResult.Status.SKIPPED) {
                        context.getLogger().debugf("No transactions subsystem XML configurations found with deprecated object store paths.");
                    } else {
                        context.getLogger().infof("Subsystem transactions XML updated.");
                    }
                    return taskResult;
                })
                .build();
    }

    protected ServerMigrationTaskResult processXMLConfiguration(final JBossServerConfiguration<S> source, final JBossServerConfiguration targetConfigurationPath, final TaskContext context) {
        // setup and run the xml filter
        final Boolean updated = false;
        ServerMigrationTaskResult.Builder taskResultBuilder = new ServerMigrationTaskResult.Builder().skipped();
        final XMLFileFilter extensionsFilter = (startElement, xmlEventReader, xmlEventWriter, xmlEventFactory) -> {
            if (startElement.getName().getLocalPart().equals("subsystem") && startElement.getName().getNamespaceURI().startsWith("urn:jboss:domain:transactions:")) {
                try {
                    xmlEventWriter.add(startElement);
                    if (processSubsystemXMLConfiguration(xmlEventReader, xmlEventWriter, xmlEventFactory)) {
                        taskResultBuilder.success();
                    }
                } catch (Throwable e) {
                    throw new ServerMigrationFailureException(e);
                }
                return XMLFileFilter.Result.CONTINUE;
            } else {
                return XMLFileFilter.Result.NOT_APPLICABLE;
            }
        };
        XMLFiles.filter(targetConfigurationPath.getPath(), extensionsFilter);
        return taskResultBuilder.build();
    }

    protected boolean processSubsystemXMLConfiguration(XMLEventReader xmlEventReader, XMLEventWriter xmlEventWriter, XMLEventFactory xmlEventFactory) throws XMLStreamException {
        boolean updated = false;
        String path = null;
        String relativeTo = null;
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                final StartElement startElement = xmlEvent.asStartElement();
                final QName qName = startElement.getName();
                if (qName.getLocalPart().equals("core-environment")) {
                    // write element without path and relativeTo attributes
                    final List<Attribute> attributes = new ArrayList<>();
                    final Iterator<Attribute> iterator = startElement.getAttributes();
                    while(iterator.hasNext()) {
                        Attribute attribute = iterator.next();
                        if (attribute.getName().getLocalPart().equals("path")) {
                            path = attribute.getValue();
                        } else if (attribute.getName().getLocalPart().equals("relative-to")) {
                            relativeTo = attribute.getValue();
                        } else {
                            attributes.add(attribute);
                        }
                    }
                    final StartElement startElementToWrite;
                    if (path != null || relativeTo != null) {
                        // write element without path and relativeTo attributes
                        startElementToWrite = xmlEventFactory.createStartElement(qName, attributes.iterator(), startElement.getNamespaces());
                    } else {
                        // write original
                        startElementToWrite = startElement;
                    }
                    writeElement(startElementToWrite, xmlEventReader, xmlEventWriter);
                } else {
                    if (updated) {
                        // update done, write original element
                        writeElement(startElement, xmlEventReader, xmlEventWriter);
                    } else {
                        if (path == null && relativeTo == null) {
                            // update not needed, write original element
                            writeElement(startElement, xmlEventReader, xmlEventWriter);
                        } else {
                            if (qName.getLocalPart().equals("object-store")) {
                                // object-store element already exists and should prevail, write original and consider update done
                                writeElement(startElement, xmlEventReader, xmlEventWriter);
                                updated = true;
                            } else if (qName.getLocalPart().equals("jts") || qName.getLocalPart().equals("use-hornetq-store") || qName.getLocalPart().equals("jdbc-store") || qName.getLocalPart().equals("commit-markable-resources")) {
                                // create and write object-store element, current element should only be written after
                                writeNewObjectStoreElement(path, relativeTo, qName, startElement.getNamespaces(), xmlEventWriter, xmlEventFactory);
                                updated = true;
                                // now write current element
                                writeElement(startElement, xmlEventReader, xmlEventWriter);
                            } else {
                                // update to do but it's an element before object-store, write original element
                                writeElement(startElement, xmlEventReader, xmlEventWriter);
                            }
                        }
                    }
                }
            } else {
                if (xmlEvent.isEndElement()) {
                    // subsystem end element and update not done
                    final EndElement endElement = xmlEvent.asEndElement();
                    if (!updated && (path != null || relativeTo != null)) {
                        writeNewObjectStoreElement(path, relativeTo, endElement.getName(), endElement.getNamespaces(), xmlEventWriter, xmlEventFactory);
                        updated = true;
                    }
                    // now write subsystem end element
                    xmlEventWriter.add(xmlEvent);
                    return updated;
                } else {
                    // not a start or end element, write original
                    xmlEventWriter.add(xmlEvent);
                }
            }
        }
        throw new IllegalStateException();
    }

    protected void writeElement(StartElement startElement, XMLEventReader xmlEventReader, XMLEventWriter xmlEventWriter) throws XMLStreamException {
        xmlEventWriter.add(startElement);
        int endElementsLeft = 1;
        do {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                endElementsLeft++;
            }
            else if(xmlEvent.isEndElement()){
                endElementsLeft--;
            }
            xmlEventWriter.add(xmlEvent);
        } while (xmlEventReader.hasNext() && endElementsLeft > 0);
    }

    protected void writeNewObjectStoreElement(String path, String relativeTo, QName currentElementQName, Iterator currentElementNamespaces, XMLEventWriter xmlEventWriter, XMLEventFactory xmlEventFactory) throws XMLStreamException {
        final List<Attribute> attributes = new ArrayList<>();
        if (path != null) {
            attributes.add(xmlEventFactory.createAttribute("path", path));
        }
        if (relativeTo != null) {
            attributes.add(xmlEventFactory.createAttribute("relative-to", relativeTo));
        }
        final QName objectStoreQName = currentElementQName.getPrefix() == null ? new QName(currentElementQName.getNamespaceURI(),"object-store") : new QName(currentElementQName.getNamespaceURI(),"object-store", currentElementQName.getPrefix());
        xmlEventWriter.add(xmlEventFactory.createStartElement(objectStoreQName, attributes.iterator(), currentElementNamespaces));
        xmlEventWriter.add(xmlEventFactory.createEndElement(objectStoreQName, currentElementNamespaces));
    }
}