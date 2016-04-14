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
import org.wildfly.migration.core.util.MigrationFiles;

import java.util.Properties;

/**
 * The server migration execution's context.
 * @author emmartins
 */
public interface ServerMigrationContext {
    /**
     * Retrieves the migration's console.
     * @return the migration's console
     */
    ConsoleWrapper getConsoleWrapper();
    /**
     * Retreives the migration files
     * @return the migration files
     */
    MigrationFiles getMigrationFiles();

    /**
     * Indicates if the migration is interactive.
     * @return true if the migration is interactive, false otherwise
     */
    boolean isInteractive();

    /**
     * Retrieves the user's environment, used to customize the migration process.
     * @return the user's environment
     */
    Properties getUserEnvironment();
}
