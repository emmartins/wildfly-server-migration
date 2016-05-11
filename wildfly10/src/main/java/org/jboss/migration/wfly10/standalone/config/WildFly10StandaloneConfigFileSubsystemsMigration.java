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
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Migration logic of WildFly 10 Subsystems, and related Extension.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSubsystemsMigration<S extends Server> {

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("subsystems-migration").build();
    public static final String SERVER_MIGRATION_TASK_ID_NAME_REMOVE_UNSUPPORTED_SUBSYSTEM = "remove-unsupported-subsystem";
    public static final String SERVER_MIGRATION_TASK_ID_NAME_REMOVE_UNSUPPORTED_EXTENSION = "remove-unsupported-extension";
    public static final String SERVER_MIGRATION_TASK_ID_ATTRIBUTE_NAME = "name";

    private final List<WildFly10Extension> supportedExtensions;
    private final List<WildFly10Subsystem> supportedSubsystems;

    public WildFly10StandaloneConfigFileSubsystemsMigration(List<WildFly10Extension> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
        this.supportedSubsystems = getSupportedSubsystems(supportedExtensions);
    }

    private static List<WildFly10Subsystem> getSupportedSubsystems(List<WildFly10Extension> supportedExtensions) {
        List<WildFly10Subsystem> supported = new ArrayList<>();
        for (WildFly10Extension extension : supportedExtensions) {
            supported.addAll(extension.getSubsystems());
        }
        return Collections.unmodifiableList(supported);
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskId getServerMigrationTaskId() {
        return SERVER_MIGRATION_TASK_ID;
    }

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> source, final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return getServerMigrationTaskId();
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
                    final Set<String> subsystems = target.getSubsystems();
                    context.getLogger().debugf("Subsystems found: %s", subsystems);
                    // delete subsystems/extensions not supported
                    removeUnsupportedSubsystems(target, subsystems, context);
                    final Set<String> extensions = target.getExtensions();
                    context.getLogger().debugf("Extensions found: %s", target.getExtensions());
                    removeUnsupportedExtensions(target, extensions, context);
                    // migrate extensions/subsystems
                    migrateExtensions(target, extensions, context);
                } finally {
                    if (!targetStarted) {
                        target.stop();
                    }
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    protected void removeUnsupportedSubsystems(final WildFly10StandaloneServer wildFly10StandaloneServer, final Set<String> subsystems, final ServerMigrationTaskContext context) throws IOException {
        for (final String subsystem : subsystems) {
            boolean supported = false;
            for (WildFly10Subsystem supportedSubsystem : supportedSubsystems) {
                if (subsystem.equals(supportedSubsystem.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                final ServerMigrationTaskId subtaskId = new ServerMigrationTaskId.Builder().setName(SERVER_MIGRATION_TASK_ID_NAME_REMOVE_UNSUPPORTED_SUBSYSTEM).addAttribute(SERVER_MIGRATION_TASK_ID_ATTRIBUTE_NAME, subsystem).build();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskId getId() {
                        return subtaskId;
                    }
                    @Override
                    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                        wildFly10StandaloneServer.removeSubsystem(subsystem);
                        context.getLogger().infof("Unsupported subsystem %s removed.", subsystem);
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }

    protected void removeUnsupportedExtensions(final WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensions, ServerMigrationTaskContext context) throws IOException {
        for (final String extension : extensions) {
            boolean supported = false;
            for (WildFly10Extension supportedExtension : supportedExtensions) {
                if (extension.equals(supportedExtension.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                final ServerMigrationTaskId subtaskId = new ServerMigrationTaskId.Builder().setName(SERVER_MIGRATION_TASK_ID_NAME_REMOVE_UNSUPPORTED_EXTENSION).addAttribute(SERVER_MIGRATION_TASK_ID_ATTRIBUTE_NAME, extension).build();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskId getId() {
                        return subtaskId;
                    }
                    @Override
                    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                        wildFly10StandaloneServer.removeExtension(extension);
                        context.getLogger().infof("Unsupported extension %s removed.", extension);
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }

    protected void migrateExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensions, ServerMigrationTaskContext context) throws IOException {
        for (WildFly10Extension supportedExtension : supportedExtensions) {
            //if (extensions.contains(supportedExtension.getName())) {
                supportedExtension.migrate(wildFly10StandaloneServer, context);
            //}
        }
    }
}
