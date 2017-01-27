/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.core.report;

import org.jboss.logging.Logger;
import org.jboss.migration.core.MigrationData;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.task.TaskExecution;
import org.jboss.migration.core.util.xml.AttributeValue;
import org.jboss.migration.core.util.xml.ElementNode;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.jboss.staxmapper.XMLMapper;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * The XML report writer.
 * @author emmartins
 */
public class XmlReportWriter implements XMLElementWriter<MigrationData> {

    public static XmlReportWriter INSTANCE = new XmlReportWriter();

    private XmlReportWriter() {

    }

    public void writeContent(XMLStreamWriter streamWriter, MigrationData value) throws XMLStreamException {
        final XMLMapper mapper = XMLMapper.Factory.create();
        mapper.deparseDocument(this, value, streamWriter);
    }

    public void writeContent(File file, MigrationData value) throws XMLStreamException, IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            try {
                writeContent(writer, value);
            } finally {
                try {
                    writer.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter streamWriter, MigrationData description) throws XMLStreamException {
        // build node tree
        final ElementNode rootElementNode = new ElementNode(null, "server-migration-report", "urn:jboss:server-migration:1.0");
        processMigrationData(description, rootElementNode);
        // write the xml
        streamWriter.writeStartDocument();
        rootElementNode.marshall(streamWriter);
        streamWriter.writeEndDocument();
    }

    protected void processMigrationData(MigrationData description, ElementNode rootElementNode) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date(description.getRootTask().getStartTime()));
        rootElementNode.addAttribute("start-time", new AttributeValue(utcTime));
        final ElementNode serversNode = new ElementNode(rootElementNode, "servers");
        processServer(description.getSource(), serversNode, "source");
        processServer(description.getTarget(), serversNode, "target");
        rootElementNode.addChild(serversNode);
        processEnvironment(description.getServerMigrationEnvironment(), rootElementNode);
        processTask(description.getRootTask(), rootElementNode);
    }

    protected void processServer(Server server, ElementNode parentElementNode, String elementLocalName) {
        final ElementNode serverNode = new ElementNode(parentElementNode, elementLocalName);
        serverNode.addAttribute("name", new AttributeValue(server.getProductInfo().getName()));
        serverNode.addAttribute("version", new AttributeValue(server.getProductInfo().getVersion()));
        serverNode.addAttribute("base-dir", new AttributeValue(server.getBaseDir().toString()));
        parentElementNode.addChild(serverNode);
    }

    protected void processEnvironment(MigrationEnvironment environment, ElementNode parentElementNode) {
        final ElementNode environmentNode = new ElementNode(parentElementNode, "environment");
        for (String propertyName : environment.getPropertyNamesReaded()) {
            final ElementNode propertyNode = new ElementNode(environmentNode, "property");
            propertyNode.addAttribute("name", new AttributeValue(propertyName));
            propertyNode.addAttribute("value", new AttributeValue(environment.getPropertyAsString(propertyName, "null")));
            environmentNode.addChild(propertyNode);
        }
        parentElementNode.addChild(environmentNode);
    }


    protected void processTask(TaskExecution task, ElementNode parentElementNode) {
        final ElementNode taskNode = new ElementNode(parentElementNode, "task");
        taskNode.addAttribute("number", new AttributeValue(String.valueOf(task.getTaskNumber())));
        taskNode.addAttribute("name", new AttributeValue(task.getTaskName().toString()));
        processTaskLogger(task.getLogger(), taskNode);
        processTaskResult(task.getResult(), taskNode);
        processSubtasks(task.getSubtasks(), taskNode);
        parentElementNode.addChild(taskNode);
    }

    protected void processTaskLogger(Logger logger, ElementNode taskNode) {
        final ElementNode loggerNode = new ElementNode(taskNode, "logger");
        loggerNode.addAttribute("logger", new AttributeValue(logger.getName()));
        taskNode.addChild(loggerNode);
    }

    protected void processTaskResult(ServerMigrationTaskResult result, ElementNode taskNode) {
        final ElementNode resultNode = new ElementNode(taskNode, "result");
        resultNode.addAttribute("status", new AttributeValue(result.getStatus().name()));
        if (result.getFailReason() != null) {
            resultNode.addAttribute("fail-reason", new AttributeValue(result.getFailReason().toString()));
        }
        final Map<String, String> attributes = result.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            final ElementNode attributesElementNode = new ElementNode(resultNode, "attributes");
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                final ElementNode attributeElementNode = new ElementNode(resultNode, "attribute");
                attributeElementNode.addAttribute("name", new AttributeValue(attribute.getKey()));
                attributeElementNode.addAttribute("value", new AttributeValue(attribute.getValue()));
                attributesElementNode.addChild(attributeElementNode);
            }
            resultNode.addChild(attributesElementNode);
        }
        taskNode.addChild(resultNode);
    }

    protected void processSubtasks(List<TaskExecution> subtasks, ElementNode taskNode) {
        if (subtasks != null && !subtasks.isEmpty()) {
            final ElementNode subtasksNode = new ElementNode(taskNode, "subtasks");
            for (TaskExecution subtask : subtasks) {
                processTask(subtask, subtasksNode);
            }
            taskNode.addChild(subtasksNode);
        }
    }
}
