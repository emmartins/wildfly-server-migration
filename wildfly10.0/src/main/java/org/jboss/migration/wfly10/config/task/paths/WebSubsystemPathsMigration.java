/*
 * Copyright 2018 Red Hat, Inc.
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
import java.util.HashSet;
import java.util.Set;

/**
 * @author emmartins
 */
public class WebSubsystemPathsMigration implements XmlConfigurationMigration.Component {

    /**
     *
     */
    public static class Factory implements XmlConfigurationMigration.ComponentFactory {
        @Override
        public XmlConfigurationMigration.Component newComponent() {
            return new WebSubsystemPathsMigration();
        }
    }

    public static final Set<String> ELEMENT_LOCAL_NAMES = Collections.singleton("ssl");

    public static final String CERTIFICATE_KEY_FILE = "certificate-key-file";
    public static final String CA_CERTIFICATE_FILE = "ca-certificate-file";

    protected final Set<String> certificateKeyFiles;
    protected final Set<String> caCertificateFiles;

    protected WebSubsystemPathsMigration() {
        certificateKeyFiles = new HashSet<>();
        caCertificateFiles = new HashSet<>();
    }

    @Override
    public Set<String> getElementLocalNames() {
        return ELEMENT_LOCAL_NAMES;
    }

    @Override
    public void processElement(XMLStreamReader reader, JBossServerConfiguration sourceConfiguration, JBossServerConfiguration targetConfiguration, TaskContext context) throws IOException {
        final String namespaceURI = reader.getNamespaceURI();
        if (namespaceURI == null || !namespaceURI.startsWith("urn:jboss:domain:web:")) {
            return;
        }
        final String certificateKeyFile = reader.getAttributeValue(null, CERTIFICATE_KEY_FILE);
        if (certificateKeyFile != null) {
            certificateKeyFiles.add(certificateKeyFile);
        }
        final String caCertificateFile = reader.getAttributeValue(null, CA_CERTIFICATE_FILE);
        if (caCertificateFile != null) {
            caCertificateFiles.add(caCertificateFile);
        }
    }

    @Override
    public void afterProcessingElements(JBossServerConfiguration sourceConfiguration, JBossServerConfiguration targetConfiguration, TaskContext taskContext) {
        taskContext.execute(new SimpleComponentTask.Builder()
                .name(taskContext.getTaskName().getName()+".subsystem.web.ssl")
                .skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet())
                .runnable(context -> {
                    final String subtaskNamePrefix = context.getTaskName()+".";
                    for (String certificateKeyFile : certificateKeyFiles) {
                        context.execute(new MigrateResolvablePathTaskBuilder()
                                .name(subtaskNamePrefix+CERTIFICATE_KEY_FILE)
                                .path(ResolvablePath.fromPathExpression(certificateKeyFile))
                                .source(sourceConfiguration)
                                .target(targetConfiguration)
                                .build());
                    }
                    for (String caCertificateFile : caCertificateFiles) {
                        context.execute(new MigrateResolvablePathTaskBuilder()
                                .name(subtaskNamePrefix+CA_CERTIFICATE_FILE)
                                .path(ResolvablePath.fromPathExpression(caCertificateFile))
                                .source(sourceConfiguration)
                                .target(targetConfiguration)
                                .build());
                    }
                    return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;})
                .build());
    }
}
