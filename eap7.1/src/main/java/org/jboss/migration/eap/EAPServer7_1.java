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
package org.jboss.migration.eap;

import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.wfly10.ServiceLoaderWildFlyServerMigrations10;
import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.WildFlyServerMigrations10;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServer10_1;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * The EAP 7.1 {@link Server}
 * @author emmartins
 */
public class EAPServer7_1 extends WildFlyFullServer10_1 {

    /**
     * the server migrations exclusive to EAP 7.1
     */
    private static final WildFlyServerMigrations10 SERVER_MIGRATIONS = new ServiceLoaderWildFlyServerMigrations10<>(ServiceLoader.load(EAPServerMigrationProvider7_1.class));

    public EAPServer7_1(String migrationName, ProductInfo productInfo, Path baseDir, MigrationEnvironment migrationEnvironment) {
        super(migrationName, productInfo, baseDir, migrationEnvironment);
    }

    @Override
    protected WildFlyServerMigration10 getMigration(Server source) {
        final WildFlyServerMigration10 serverMigration = SERVER_MIGRATIONS.getMigrationFrom(source);
        return serverMigration != null ? serverMigration : super.getMigration(source);
    }
}
