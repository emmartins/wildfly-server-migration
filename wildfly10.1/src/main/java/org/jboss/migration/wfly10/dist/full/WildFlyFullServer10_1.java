/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.migration.wfly10.dist.full;

import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.wfly10.ServiceLoaderWildFlyServerMigrations10;
import org.jboss.migration.wfly10.WildFlyServerMigrations10;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * @author emmartins
 */
public class WildFlyFullServer10_1 extends WildFlyFullServer10_0 {

    private static final WildFlyServerMigrations10 SERVER_MIGRATIONS = new ServiceLoaderWildFlyServerMigrations10<>(ServiceLoader.load(WildFlyFullServerMigrationProvider10_1.class));

    public WildFlyFullServer10_1(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment) {
        super(migrationName, productInfo, baseDir, migrationEnvironment, SERVER_MIGRATIONS);
    }

    protected WildFlyFullServer10_1(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment, WildFlyServerMigrations10 serverMigrations) {
        super(migrationName, productInfo, baseDir, migrationEnvironment, serverMigrations);
    }

    protected WildFlyFullServer10_1(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment, JBossServer.Extensions extensions, WildFlyServerMigrations10 serverMigrations) {
        super(migrationName, productInfo, baseDir, migrationEnvironment, extensions, serverMigrations);
    }
}
