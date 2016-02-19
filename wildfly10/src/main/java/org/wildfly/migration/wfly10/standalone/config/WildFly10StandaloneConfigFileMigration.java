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
package org.wildfly.migration.wfly10.standalone.config;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerPath;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.WildFly10Server;
import org.wildfly.migration.wfly10.standalone.EmbeddedWildFly10StandaloneServer;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract implementation of a standalone config file migration.
 * @author emmartins
 */
public abstract class WildFly10StandaloneConfigFileMigration<S extends Server> {

    public void run(ServerPath<S> sourceConfig, WildFly10Server target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating standalone server configuration %s", sourceConfig.getPath());
        copyFileToTargetServer(sourceConfig.getPath(), target, context);
        final WildFly10StandaloneServer standaloneServer = startEmbeddedServer(sourceConfig.getPath(), target, context);
        run(sourceConfig, standaloneServer, context);
        // shutdown server
        context.getConsoleWrapper().printf("%n%n");
        standaloneServer.stop();
        ServerMigrationLogger.ROOT_LOGGER.info("Standalone server configuration file migration done.");
    }

    protected abstract void run(ServerPath<S> sourceConfig, WildFly10StandaloneServer standaloneServer, ServerMigrationContext context) throws IOException;

    protected void copyFileToTargetServer(Path source, WildFly10Server targetServer, ServerMigrationContext context) throws IOException {
        // check if server file exists
        final Path target = targetServer.getStandaloneConfigurationDir().resolve(source.getFileName());
        ServerMigrationLogger.ROOT_LOGGER.debugf("Source server configuration file is %s", source);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Target server configuration file is %s", target);
        context.getMigrationFiles().copy(source, target);
        ServerMigrationLogger.ROOT_LOGGER.infof("Server configuration file %s copied to %s", source, target);
    }

    protected WildFly10StandaloneServer startEmbeddedServer(Path source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        final String config = source.getFileName().toString();
        ServerMigrationLogger.ROOT_LOGGER.infof("Starting server server configuration %s", config);
        final WildFly10StandaloneServer wildFly10StandaloneServer = new EmbeddedWildFly10StandaloneServer(config, target);
        wildFly10StandaloneServer.start();
        return wildFly10StandaloneServer;
    }
}
