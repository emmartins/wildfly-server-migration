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

import java.nio.file.Path;

/**
 * A migration source and/or target server.
 * @author emmartins
 */
public interface Server {

    /**
     * Retrieves the migration server's name.
     * @return
     */
    String getMigrationName();

    /**
     * Retrieves the server's base directory.
     * @return the server's base directory
     */
    Path getBaseDir();

    /**
     * Retrieves the server's product info.
     * @return the server's product info
     */
    ProductInfo getProductInfo();

    /**
     * Migrates from the specified source server.
     * @param source the server to migrate from
     * @param context the server migration task context
     * @return the server migration task result
     * @throws IllegalArgumentException if the server is not able to migrate from the specified source
     */
    ServerMigrationTaskResult migrate(Server source, ServerMigrationTaskContext context) throws IllegalArgumentException;

    /**
     * Resolves a path.
     * @param path the path's name
     * @return the resolved path if the server is able to resolve it's name, null otherwise
     */
    Path resolvePath(String path);
}
