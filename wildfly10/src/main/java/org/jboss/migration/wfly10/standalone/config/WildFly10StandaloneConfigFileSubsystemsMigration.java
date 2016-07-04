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
package org.jboss.migration.wfly10.standalone.config;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.util.xml.XMLFileFilter;
import org.jboss.migration.core.util.xml.XMLFiles;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.EnvironmentProperties;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Migration logic of WildFly 10 Subsystems, and related Extension.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSubsystemsMigration<S extends Server> {

    public static final ServerMigrationTaskName XML_CONFIG_SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("subsystems-xml-config").build();
    public static final ServerMigrationTaskName MANAGEMENT_RESOURCES_SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("subsystems-management-resources").build();
    public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_SUBSYSTEM = "remove-subsystem";
    public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_EXTENSION = "remove-extension";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_MODULE = "module";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAMESPACE = "namespace";

    private final List<WildFly10Extension> supportedExtensions;

    public WildFly10StandaloneConfigFileSubsystemsMigration(List<WildFly10Extension> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }

    public ServerMigrationTask getXmlConfigServerMigrationTask(final ServerPath<S> sourceConfig, final Path targetConfigFilePath, final WildFly10Server target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return XML_CONFIG_SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Processing subsystems XML config...");
                final List<WildFly10Extension> migrationExtensions = getMigrationExtensions(context.getServerMigrationContext().getMigrationEnvironment());
                final List<WildFly10Subsystem> migrationSubsystems = getMigrationSubsystems(migrationExtensions, context.getServerMigrationContext().getMigrationEnvironment());
                removeExtensionsAndSubsystems(sourceConfig, targetConfigFilePath, target, migrationExtensions, migrationSubsystems, context);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    public ServerMigrationTask getManagementResourcesServerMigrationTask(final Path targetConfigFilePath, final WildFly10StandaloneServer standaloneServer) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return MANAGEMENT_RESOURCES_SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Processing subsystems management resources...");
                final List<WildFly10Extension> migrationExtensions = getMigrationExtensions(context.getServerMigrationContext().getMigrationEnvironment());
                migrateExtensions(standaloneServer, migrationExtensions, context);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    protected void removeExtensionsAndSubsystems(final ServerPath<S> source, final Path targetConfigFilePath, final WildFly10Server targetServer, final List<WildFly10Extension> migrationExtensions, final List<WildFly10Subsystem> migrationSubsystems, final ServerMigrationTaskContext context) throws IOException {
        // setup the extensions filter
        final XMLFileFilter extensionsFilter = new XMLFileFilter() {
            @Override
            public Result filter(StartElement startElement, XMLEventReader xmlEventReader, XMLEventWriter xmlEventWriter) throws IOException {
                if (startElement.getName().getLocalPart().equals("extension")) {
                    Attribute moduleAttr = startElement.getAttributeByName(new QName("module"));
                    final String moduleName = moduleAttr.getValue();
                    // keep if module matches a supported extension name
                    for (WildFly10Extension extension : migrationExtensions) {
                        if (extension.getName().equals(moduleName)) {
                            return Result.KEEP;
                        }
                    }
                    // not supported, remove it
                    final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_REMOVE_EXTENSION).addAttribute(SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_MODULE, moduleName).build();
                    final ServerMigrationTask subtask = new ServerMigrationTask() {
                        @Override
                        public ServerMigrationTaskName getName() {
                            return subtaskName;
                        }

                        @Override
                        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                            context.getLogger().infof("Extension with module %s removed.", moduleName);
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    };
                    context.execute(subtask);
                    return Result.REMOVE;
                } else {
                    return Result.NOT_APPLICABLE;
                }
            }
        };
        // setup subsystems filter
        final XMLFileFilter subsystemsFilter = new XMLFileFilter() {
            @Override
            public Result filter(StartElement startElement, XMLEventReader xmlEventReader, XMLEventWriter xmlEventWriter) throws IOException {
                if (startElement.getName().getLocalPart().equals("subsystem")) {
                    final String namespaceURI = startElement.getName().getNamespaceURI();
                    // keep if the namespace uri starts with a supported subsystem's namespace without version
                    for (WildFly10Subsystem subsystem : migrationSubsystems) {
                        final String namespaceWithoutVersion = subsystem.getNamespaceWithoutVersion();
                        if (namespaceWithoutVersion != null && namespaceURI.startsWith(namespaceWithoutVersion + ':')) {
                            return Result.KEEP;
                        }
                    }
                    // not supported, remove subsystem
                    final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_REMOVE_SUBSYSTEM).addAttribute(SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAMESPACE, namespaceURI).build();
                    final ServerMigrationTask subtask = new ServerMigrationTask() {
                        @Override
                        public ServerMigrationTaskName getName() {
                            return subtaskName;
                        }

                        @Override
                        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                            context.getLogger().infof("Subsystem with namespace %s removed.", namespaceURI);
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    };
                    context.execute(subtask);
                    return Result.REMOVE;
                } else {
                    return Result.NOT_APPLICABLE;
                }
            }
        };
        XMLFiles.filter(targetConfigFilePath, extensionsFilter, subsystemsFilter);
    }

    protected void migrateExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, List<WildFly10Extension> extensionsToMigrate, ServerMigrationTaskContext context) throws IOException {
        for (WildFly10Extension extensionToMigrate : extensionsToMigrate) {
            extensionToMigrate.migrate(wildFly10StandaloneServer, context);
        }
    }

    private List<WildFly10Extension> getMigrationExtensions(MigrationEnvironment migrationEnvironment) {
        final List<String> removedByEnv = migrationEnvironment.getPropertyAsList(EnvironmentProperties.EXTENSIONS_REMOVE);
        if (removedByEnv == null || removedByEnv.isEmpty()) {
            return supportedExtensions;
        } else {
            final List<WildFly10Extension> migrationExtensions = new ArrayList<>();
            for (WildFly10Extension supportedExtension : supportedExtensions) {
                if (!removedByEnv.contains(supportedExtension.getName())) {
                    migrationExtensions.add(supportedExtension);
                }
            }
            return migrationExtensions;
        }
    }

    private List<WildFly10Subsystem> getMigrationSubsystems(List<WildFly10Extension> migrationExtensions, MigrationEnvironment migrationEnvironment) {
        final List<String> removedByEnv = migrationEnvironment.getPropertyAsList(EnvironmentProperties.SUBSYSTEMS_REMOVE);
        List<WildFly10Subsystem> migrationSubsystems = new ArrayList<>();
        for (WildFly10Extension extension : migrationExtensions) {
            for (WildFly10Subsystem subsystem : extension.getSubsystems()) {
                if (removedByEnv == null || !removedByEnv.contains(subsystem.getName())) {
                    migrationSubsystems.add(subsystem);
                }
            }
        }
        return migrationSubsystems;
    }
}
