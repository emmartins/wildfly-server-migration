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
package org.wildfly.migration.core;

import org.wildfly.migration.core.console.ConsoleWrapper;
import org.wildfly.migration.core.console.JavaConsole;
import org.wildfly.migration.core.logger.ServerMigrationLogger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author emmartins
 */
public class ServerMigration {

    private Path from;
    private Path to;
    private boolean interactive = true;

    public ServerMigration from(Path path) {
        this.from = path;
        return this;
    }

    public ServerMigration to(Path path) {
        this.to = path;
        return this;
    }

    public ServerMigration interactive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    public void run() throws IOException {
        if (from == null) {
            throw new IllegalStateException("Migration source server base dir not set");
        }
        if (to == null) {
            throw new IllegalStateException("Migration target server base dir not set");
        }

        final ConsoleWrapper console = new JavaConsole();

        console.printf("%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("----  JBoss Server Migration Tool  -----------------------%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("%n");

        console.printf("Retrieving servers...%n");
        final Server sourceServer = getServer("SOURCE", from);
        final Server targetServer = getServer("TARGET", to);

        console.printf("%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("----------------------------------------------------------%n");
        console.printf("%n");

        targetServer.migrate(sourceServer, new ServerMigrationContextImpl(console, true));
    }

    protected Server getServer(String name, Path baseDir) {
        ServerMigrationLogger.ROOT_LOGGER.infof("Processing %s server's base dir %s", name, baseDir);
        final Server server = Servers.getServer(baseDir);
        if (server == null) {
            // TODO fallback to manual selection
            throw new IllegalArgumentException("Failed to identify "+name+" server.");
        } else {
            ServerMigrationLogger.ROOT_LOGGER.infof("%s server %s", name, server.getProductInfo());
        }
        return server;
    }

    private static class ServerMigrationContextImpl implements ServerMigrationContext {

        private final ConsoleWrapper consoleWrapper;
        private final boolean interactive;

        private ServerMigrationContextImpl(ConsoleWrapper consoleWrapper, boolean interactive) {
            this.consoleWrapper = consoleWrapper;
            this.interactive = interactive;
        }

        @Override
        public ConsoleWrapper getConsoleWrapper() {
            return consoleWrapper;
        }

        @Override
        public boolean isInteractive() {
            return interactive;
        }
    }
}
