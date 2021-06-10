/*
 * Copyright 2021 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task.paths;

import org.jboss.migration.core.jboss.JBossServerConfiguration;
import org.jboss.migration.core.jboss.MigrateResolvablePathTaskBuilder;
import org.jboss.migration.core.jboss.ResolvablePath;
import org.jboss.migration.core.jboss.XmlConfigurationMigration;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.SimpleComponentTask;
import org.jboss.migration.core.task.component.TaskSkipPolicy;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Migration of resolvable paths specified in XML elements.
 * @author emmartins
 */
public class ResolvablePathsMigration implements XmlConfigurationMigration.Component {

    private static final String ATTR_NAME_PATH = "path";
    private static final String ATTR_NAME_RELATIVE_TO = "relative-to";

    private final Set<String> elementLocalNames;
    private final String namespaceURIPrefix;
    private final String taskNameSuffix;
    private final boolean skipIfSourcePathDoesNotExists;
    protected final Map<String,Set<ResolvablePath>> resolvablePaths;

    public ResolvablePathsMigration(String taskNameSuffix, Set<String> elementLocalNames, String namespaceURIPrefix) {
        this(taskNameSuffix, elementLocalNames, namespaceURIPrefix, false);
    }

    public ResolvablePathsMigration(String taskNameSuffix, Set<String> elementLocalNames, String namespaceURIPrefix, boolean skipIfSourcePathDoesNotExists) {
        this.taskNameSuffix = taskNameSuffix;
        this.elementLocalNames = Collections.unmodifiableSet(elementLocalNames);
        this.namespaceURIPrefix = namespaceURIPrefix;
        this.skipIfSourcePathDoesNotExists = skipIfSourcePathDoesNotExists;
        this.resolvablePaths = new HashMap<>();
        for (String elementLocalName : elementLocalNames) {
            this.resolvablePaths.put(elementLocalName, new HashSet<>());
        }
    }

    @Override
    public Set<String> getElementLocalNames() {
        return elementLocalNames;
    }

    @Override
    public void processElement(XMLStreamReader reader, JBossServerConfiguration sourceConfiguration, JBossServerConfiguration targetConfiguration, TaskContext context) throws IOException {
        final String namespaceURI = reader.getNamespaceURI();
        if (namespaceURI == null || !namespaceURI.startsWith(namespaceURIPrefix)) {
            return;
        }
        final String path = reader.getAttributeValue(null, ATTR_NAME_PATH);
        if (path == null) {
            return;
        }
        final String relativeTo = reader.getAttributeValue(null, ATTR_NAME_RELATIVE_TO);
        resolvablePaths.get(reader.getLocalName()).add(new ResolvablePath(path, relativeTo));
    }

    @Override
    public void afterProcessingElements(JBossServerConfiguration sourceConfiguration, JBossServerConfiguration targetConfiguration, TaskContext taskContext) {
        taskContext.execute(new SimpleComponentTask.Builder()
                .name(taskContext.getTaskName().getName()+"."+taskNameSuffix)
                .skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet())
                .runnable(context -> {
                    for (Map.Entry<String, Set<ResolvablePath>> resolvablePathsEntry : resolvablePaths.entrySet()) {
                        final String subtaskNamePrefix = context.getTaskName()+"."+resolvablePathsEntry.getKey();
                        for (ResolvablePath resolvablePath : resolvablePathsEntry.getValue()) {
                            context.execute(new MigrateResolvablePathTaskBuilder()
                                    .name(subtaskNamePrefix)
                                    .path(resolvablePath)
                                    .source(sourceConfiguration)
                                    .target(targetConfiguration)
                                    .skipIfSourcePathDoesNotExists(skipIfSourcePathDoesNotExists)
                                    .build());
                        }
                    }
                    return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;})
                .build());
    }
}
