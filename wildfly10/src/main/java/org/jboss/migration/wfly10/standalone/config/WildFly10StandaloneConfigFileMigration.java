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
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.standalone.EmbeddedWildFly10StandaloneServer;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract implementation of a standalone config file migration.
 * @author emmartins
 */
public abstract class WildFly10StandaloneConfigFileMigration<S extends Server> {

    public static final String MIGRATION_REPORT_TASK_NAME = "config-file-migration";
    public static final String MIGRATION_REPORT_TASK_ATTR_SOURCE = "source";

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> sourceConfig, final WildFly10Server target) {
        final ServerMigrationTaskId taskId = new ServerMigrationTaskId.Builder().setName(MIGRATION_REPORT_TASK_NAME).addAttribute(MIGRATION_REPORT_TASK_ATTR_SOURCE, sourceConfig.getPath().toString()).build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return taskId;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                WildFly10StandaloneConfigFileMigration.this.run(sourceConfig, target, context);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    protected void run(ServerPath<S> sourceConfig, WildFly10Server target, ServerMigrationTaskContext context) throws IOException {
        final ConsoleWrapper consoleWrapper = context.getServerMigrationContext().getConsoleWrapper();
        consoleWrapper.printf("%n%n");
        context.getLogger().infof("Migrating standalone server configuration %s", sourceConfig.getPath());
        copyFileToTargetServer(sourceConfig.getPath(), target, context);
        final WildFly10StandaloneServer standaloneServer = startEmbeddedServer(sourceConfig.getPath(), target, context);
        run(sourceConfig, standaloneServer, context);
        // shutdown server
        consoleWrapper.printf("%n%n");
        standaloneServer.stop();
    }

    protected abstract void run(ServerPath<S> sourceConfig, WildFly10StandaloneServer standaloneServer, ServerMigrationTaskContext context) throws IOException;

    protected void copyFileToTargetServer(Path source, WildFly10Server targetServer, ServerMigrationTaskContext context) throws IOException {
        // check if server file exists
        final Path target = targetServer.getStandaloneConfigurationDir().resolve(source.getFileName());
        context.getLogger().debugf("Source server configuration file is %s", source);
        context.getLogger().debugf("Target server configuration file is %s", target);
        context.getServerMigrationContext().getMigrationFiles().copy(source, target);
        context.getLogger().infof("Server configuration file %s copied to %s", source, target);
    }

    protected WildFly10StandaloneServer startEmbeddedServer(Path source, WildFly10Server target, ServerMigrationTaskContext context) throws IOException {
        context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
        final String config = source.getFileName().toString();
        context.getLogger().infof("Starting server configuration %s", config);
        final WildFly10StandaloneServer wildFly10StandaloneServer = new EmbeddedWildFly10StandaloneServer(config, target);
        wildFly10StandaloneServer.start();
        return wildFly10StandaloneServer;
    }
}
