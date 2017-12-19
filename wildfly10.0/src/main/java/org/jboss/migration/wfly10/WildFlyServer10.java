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
package org.jboss.migration.wfly10;

import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public abstract class WildFlyServer10 extends JBossServer<WildFlyServer10> {

    protected final WildFlyServerMigrations10 serverMigrations;

    public WildFlyServer10(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment, JBossServer.Extensions extensions) {
        this(migrationName, productInfo, baseDir, migrationEnvironment, extensions, null);
    }

    public WildFlyServer10(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment, JBossServer.Extensions extensions, WildFlyServerMigrations10 serverMigrations) {
        super(migrationName, productInfo, baseDir, migrationEnvironment, extensions);
        this.serverMigrations = serverMigrations;
    }

    @Override
    public ServerMigrationTaskResult migrate(Server source, TaskContext context) throws IllegalArgumentException {
        final WildFlyServerMigration10 migration = getMigration(source);
        if (migration != null) {
            return migration.run(source, this, context);
        } else {
            return super.migrate(source, context);
        }
    }

    protected WildFlyServerMigration10 getMigration(Server source) {
        return serverMigrations != null ? serverMigrations.getMigrationFrom(source) : null;
    }
}
