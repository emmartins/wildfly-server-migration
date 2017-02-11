/*
 * Copyright 2017 Red Hat, Inc.
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
package org.jboss.migration.core;

import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.env.MigrationEnvironment;

/**
 * The server migration execution's context.
 * @author emmartins
 */
class ServerMigrationContextImpl implements ServerMigrationContext {

    private final ConsoleWrapper consoleWrapper;
    private final boolean interactive;
    private final MigrationFiles migrationFiles;
    private final MigrationEnvironment migrationEnvironment;

    ServerMigrationContextImpl(ConsoleWrapper consoleWrapper, boolean interactive, MigrationEnvironment migrationEnvironment) {
        this.consoleWrapper = consoleWrapper;
        this.interactive = interactive;
        this.migrationEnvironment = migrationEnvironment;
        this.migrationFiles = new MigrationFiles();
    }

    @Override
    public ConsoleWrapper getConsoleWrapper() {
        return consoleWrapper;
    }

    @Override
    public MigrationFiles getMigrationFiles() {
        return migrationFiles;
    }

    @Override
    public boolean isInteractive() {
        return interactive;
    }

    @Override
    public MigrationEnvironment getMigrationEnvironment() {
        return migrationEnvironment;
    }
}
