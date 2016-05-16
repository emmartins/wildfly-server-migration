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

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public abstract class WildFly10Server extends AbstractServer {

    public WildFly10Server(ProductInfo productInfo, Path baseDir) {
        super(productInfo, baseDir);
    }

    @Override
    public ServerMigrationTaskResult migrate(Server source, ServerMigrationTaskContext context) throws IllegalArgumentException {
        final WildFly10ServerMigration migration = getMigration(source);
        if (migration != null) {
            return migration.run(source, this, context);
        } else {
            throw new IllegalArgumentException("Source server migration to WildFly 10 not supported: "+source.getProductInfo());
        }
    }

    protected abstract WildFly10ServerMigration getMigration(Server source);

    public Path getStandaloneDir() {
        return getBaseDir().resolve("standalone");
    }

    public Path getStandaloneConfigurationDir() {
        return getStandaloneDir().resolve("configuration");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getModulesSystemLayersBaseDir() {
        return getModulesSystemLayersBaseDir(getBaseDir());
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public static Path getModulesSystemLayersBaseDir(Path baseDir) {
        return getModulesDir(baseDir).resolve("system").resolve("layers").resolve("base");
    }
}
