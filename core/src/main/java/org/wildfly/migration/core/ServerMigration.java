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

import org.wildfly.migration.core.config.ConfigurationMigration;
import org.wildfly.migration.core.server.TargetServerFactory;
import org.wildfly.migration.core.server.embedded.EmbeddedTargetServerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author emmartins
 */
public class ServerMigration {

    private Path from;
    private Path to;

    public ServerMigration from(Path path) {
        this.from = path;
        return this;
    }

    public ServerMigration to(Path path) {
        this.to = path;
        return this;
    }

    public void run() throws IOException {
        if (from == null) {
            throw new IllegalStateException("Migration source not set");
        }
        if (to == null) {
            throw new IllegalStateException("Migration server not set");
        }
        if (Files.isDirectory(from)) {
            // TODO find and migrate all configs
            throw new IllegalArgumentException("single config file is currently the only supported source");
        } else {
            ConfigurationMigration.run(from, new MigrationContextImpl(to));
        }
    }

    private static class MigrationContextImpl implements MigrationContext {

        private final TargetServerFactory targetServerFactory;

        private MigrationContextImpl(Path baseDir) {
            targetServerFactory = new EmbeddedTargetServerFactory(baseDir);
        }

        @Override
        public Prompt getPrompt() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TargetServerFactory getTargetServerFactory() {
            return targetServerFactory;
        }
    }
}
