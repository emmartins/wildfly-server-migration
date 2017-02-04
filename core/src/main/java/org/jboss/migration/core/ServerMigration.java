/*
 * Copyright 2015 Red Hat, Inc.
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
package org.jboss.migration.core;

import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.JavaConsole;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.SystemEnvironment;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.report.SummaryReportWriter;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.TaskExecutionImpl;

import java.nio.file.Path;

/**
 * The core server migration's configurator and executor.
 *
 * The configuration of the migration's source and target's base dirs is mandatory.
 *
 * The migration's execution retrieves the source and target {@link Server}s, from base dirs, creates the migration context, and then delegates the migration to the target {@link Server}.
 *
 * @author emmartins
 */
public class ServerMigration {

    private static final String SOURCE = "SOURCE";
    private static final String TARGET = "TARGET";

    private Path from;
    private Path to;
    private ConsoleWrapper console;
    private boolean interactive = true;
    private MigrationEnvironment userEnvironment;

    /**
     * Sets the migration source's base dir.
     * @param path the source server base dir
     * @return the server migration after applying the configuration change
     */
    public ServerMigration from(Path path) {
        this.from = path;
        return this;
    }

    /**
     * Sets the migration target's base dir.
     * @param path the target server base dir
     * @return the server migration after applying the configuration change
     */
    public ServerMigration to(Path path) {
        this.to = path;
        return this;
    }

    /**
     * Sets the {@link ConsoleWrapper} to be used during migration. Exposed only for testing.
     * @param console the console to use
     * @return the server migration after applying the configuration change
     */
    public ServerMigration console(ConsoleWrapper console) {
        this.console = console;
        return this;
    }

    /**
     * Specifies if the server migration execution may interact with the user.
     * By default user interaction is on.
     * @param interactive true if the server migration execution may interact with the user, false otherwise
     * @return the server migration after applying the configuration change
     */
    public ServerMigration interactive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    /**
     * Sets the user environment, used to customize the migration process.
     * @param userEnvironment the user's environment
     * @return the server migration after applying the configuration change
     */
    public ServerMigration userEnvironment(MigrationEnvironment userEnvironment) {
        this.userEnvironment = userEnvironment;
        return this;
    }

    /**
     * Executes the configured server migration, i.e. retrieves the source and target {@link Server}s, from base dirs, creates the migration context, and then delegates the migration to the target {@link Server}.
     * @throws IllegalArgumentException if a server was not retrieved from configured base dir.
     * @throws IllegalStateException if the source and/or target base dir is not configured
     * @return the migration data
     */
    public MigrationData run() throws IllegalArgumentException, IllegalStateException, ServerMigrationFailureException {
        if (from == null) {
            throw ServerMigrationLogger.ROOT_LOGGER.serverBaseDirNotSet(SOURCE);
        }
        if (to == null) {
            throw ServerMigrationLogger.ROOT_LOGGER.serverBaseDirNotSet(TARGET);
        }

        if (userEnvironment == null) {
            userEnvironment = new MigrationEnvironment();
        }
        final MigrationEnvironment migrationEnvironment = new MigrationEnvironment();
        migrationEnvironment.setProperties(userEnvironment);
        migrationEnvironment.setProperties(SystemEnvironment.INSTANCE);

        final ConsoleWrapper console = this.console != null ? this.console : new JavaConsole();

        console.printf("%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("----  JBoss Server Migration Tool  -----------------------%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("%n");

        console.printf("Retrieving servers...%n");
        final Server sourceServer = getServer(SOURCE, from, migrationEnvironment);
        final Server targetServer = getServer(TARGET, to, migrationEnvironment);

        console.printf("%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("%n");

        final ServerMigrationContext serverMigrationContext = new ServerMigrationContext(console, interactive, migrationEnvironment);
        final ServerMigrationTaskName serverMigrationTaskName = new ServerMigrationTaskName.Builder("server")
                .build();
        final ServerMigrationTask serverMigrationTask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return serverMigrationTaskName;
            }

            @Override
            public ServerMigrationTaskResult run(TaskContext context) {
                context.getServerMigrationContext().getConsoleWrapper().printf("Server migration starting...%n");
                final ServerMigrationTaskResult result = targetServer.migrate(sourceServer, context);
                context.getServerMigrationContext().getConsoleWrapper().printf("%nServer migration done.%n%n");
                return result;
            }
        };
        final TaskExecutionImpl taskExecutionImpl = new TaskExecutionImpl(serverMigrationTask, serverMigrationContext);
        try {
            taskExecutionImpl.run();
        } catch (Throwable t) {
            ServerMigrationLogger.ROOT_LOGGER.error("Migration failed", t);
        }

        // build migration data
        final MigrationData migrationData = new MigrationData(sourceServer, targetServer, taskExecutionImpl, migrationEnvironment);
        // log summary report
        ServerMigrationLogger.ROOT_LOGGER.infof(SummaryReportWriter.INSTANCE.toString(migrationData));
        return migrationData;
    }

    /**
     * Retrieves a {@link Server} from its base dir.
     * @param name the assigned server name
     * @param baseDir the base dir of the server to retrieve
     * @param migrationEnvironment
     * @return the {@link Server} from its base dir.
     * @throws IllegalArgumentException if no server was retrieved
     */
    protected Server getServer(String name, Path baseDir, MigrationEnvironment migrationEnvironment) throws IllegalArgumentException {
        baseDir = baseDir.normalize();
        ServerMigrationLogger.ROOT_LOGGER.debugf("Processing %s server's base dir %s", name, baseDir);
        final Server server = Servers.getServer(name.toLowerCase(), baseDir, migrationEnvironment);
        if (server == null) {
            // TODO support multiple servers for a single base dir
            throw ServerMigrationLogger.ROOT_LOGGER.failedToRetrieveServerFromBaseDir(name, baseDir.toString());
        } else {
            ServerMigrationLogger.ROOT_LOGGER.serverProductInfo(name, server.getProductInfo());
        }
        return server;
    }

}
