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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Migration logic of WildFly 10 Subsystems, and related Extension.
 * @author emmartins
 */
public class SubsystemsMigration<S> {

    public static final ServerMigrationTaskName MANAGEMENT_RESOURCES_SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("subsystems-management-resources").build();

    private final List<Extension> supportedExtensions;

    protected SubsystemsMigration(Builder builder) {
        this.supportedExtensions = Collections.unmodifiableList(builder.supportedExtensions);
    }

    public ServerMigrationTask getSubsystemsManagementTask(S source, final SubsystemResources subsystemResources) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return MANAGEMENT_RESOURCES_SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                //context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Subsystems resources migration starting...");
                migrateExtensions(subsystemResources, context);
                context.getLogger().infof("Subsystems resources migration done.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    protected void migrateExtensions(final SubsystemResources subsystemResources, TaskContext context) throws ServerMigrationFailureException {
        final List<Extension> extensionsToMigrate = getMigrationExtensions(context.getServerMigrationContext().getMigrationEnvironment());
        for (Extension extensionToMigrate : extensionsToMigrate) {
            extensionToMigrate.migrate(subsystemResources, context);
        }
    }

    private List<Extension> getMigrationExtensions(MigrationEnvironment migrationEnvironment) {
        final List<String> removedByEnv = migrationEnvironment.getPropertyAsList(EnvironmentProperties.EXTENSIONS_REMOVE);
        if (removedByEnv == null || removedByEnv.isEmpty()) {
            return supportedExtensions;
        } else {
            final List<Extension> migrationExtensions = new ArrayList<>();
            for (Extension supportedExtension : supportedExtensions) {
                if (!removedByEnv.contains(supportedExtension.getName())) {
                    migrationExtensions.add(supportedExtension);
                }
            }
            return migrationExtensions;
        }
    }

    public static class Builder<S> {

        private final List<Extension> supportedExtensions = new ArrayList<>();

        public Builder<S> addExtension(Extension extension) {
            supportedExtensions.add(extension);
            return this;
        }

        public Builder<S> addExtension(ExtensionBuilder extensionBuilder) {
            supportedExtensions.add(extensionBuilder.build());
            return this;
        }

        public Builder<S> addExtensions(Collection<Extension> extensions) {
            supportedExtensions.addAll(extensions);
            return this;
        }

        public SubsystemsMigration<S> build() {
            return new SubsystemsMigration<S>(this);
        }
    }
}