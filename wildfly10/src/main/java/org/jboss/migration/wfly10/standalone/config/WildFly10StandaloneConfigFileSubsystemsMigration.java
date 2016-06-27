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
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.EnvironmentProperties;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Migration logic of WildFly 10 Subsystems, and related Extension.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSubsystemsMigration<S extends Server> {

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("subsystems").build();
    public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_SUBSYSTEM = "remove-subsystem";
    public static final String SERVER_MIGRATION_TASK_NAME_REMOVE_EXTENSION = "remove-extension";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAME = "name";

    private final List<WildFly10Extension> supportedExtensions;

    public WildFly10StandaloneConfigFileSubsystemsMigration(List<WildFly10Extension> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskName getServerMigrationTaskName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> source, final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return getServerMigrationTaskName();
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Migrating subsystems...");
                final boolean targetStarted = target.isStarted();
                if (!targetStarted) {
                    target.start();
                }
                try {
                    final List<WildFly10Extension> migrationExtensions = getMigrationExtensions(context.getServerMigrationContext().getMigrationEnvironment());
                    removeSubsystems(target, target.getSubsystems(), migrationExtensions, context);
                    removeExtensions(target, target.getExtensions(), migrationExtensions, context);
                    migrateExtensions(target, migrationExtensions, context);
                } finally {
                    if (!targetStarted) {
                        target.stop();
                    }
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
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

    protected void removeSubsystems(final WildFly10StandaloneServer wildFly10StandaloneServer, final Set<String> subsystemsInConfig, List<WildFly10Extension> migrationExtensions, final ServerMigrationTaskContext context) throws IOException {
        final List<WildFly10Subsystem> subsystemsToKeep = getMigrationSubsystems(migrationExtensions, context.getServerMigrationContext().getMigrationEnvironment());
        for (final String subsystemInConfig : subsystemsInConfig) {
            boolean remove = true;
            for (WildFly10Subsystem subsystemToKeep : subsystemsToKeep) {
                if (subsystemInConfig.equals(subsystemToKeep.getName())) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_REMOVE_SUBSYSTEM).addAttribute(SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAME, subsystemInConfig).build();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskName getName() {
                        return subtaskName;
                    }
                    @Override
                    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                        wildFly10StandaloneServer.removeSubsystem(subsystemInConfig);
                        context.getLogger().infof("Subsystem %s removed.", subsystemInConfig);
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }

    protected void removeExtensions(final WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensionsInConfig, List<WildFly10Extension> extensionsToKeep, ServerMigrationTaskContext context) throws IOException {
        for (final String extensionInConfig : extensionsInConfig) {
            boolean remove = true;
            for (WildFly10Extension extensionToKeep : extensionsToKeep) {
                if (extensionInConfig.equals(extensionToKeep.getName())) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_REMOVE_EXTENSION).addAttribute(SERVER_MIGRATION_TASK_NAME_ATTRIBUTE_NAME, extensionInConfig).build();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskName getName() {
                        return subtaskName;
                    }
                    @Override
                    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                        wildFly10StandaloneServer.removeExtension(extensionInConfig);
                        context.getLogger().infof("Extension %s removed.", extensionInConfig);
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }

    protected void migrateExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, List<WildFly10Extension> extensionsToMigrate, ServerMigrationTaskContext context) throws IOException {
        for (WildFly10Extension extensionToMigrate : extensionsToMigrate) {
            extensionToMigrate.migrate(wildFly10StandaloneServer, context);
        }
    }
}
