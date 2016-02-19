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

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerPath;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSecurityRealmsMigration<S extends Server> {

    public void run(ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating security realms...");
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            for (ModelNode securityRealm : target.getSecurityRealms()) {
                migrateSecurityRealm(securityRealm, source, target, context);
            }
        } finally {
            if (!targetStarted) {
                target.stop();
            }
            ServerMigrationLogger.ROOT_LOGGER.info("Security realms migration done.");
        }
    }

    protected void migrateSecurityRealm(ModelNode securityRealm, ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        final Property securityRealmProperty = securityRealm.asProperty();
        final String securityRealmName = securityRealmProperty.getName();
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating security realm: %s", securityRealmName);
        final ModelNode securityRealmValue = securityRealmProperty.getValue();
        if (securityRealmValue.hasDefined(AUTHENTICATION, PROPERTIES)) {
            copyPropertiesFile(securityRealmValue.get(AUTHENTICATION, PROPERTIES), source, target, context);
        }
        if (securityRealmValue.hasDefined(AUTHORIZATION, PROPERTIES)) {
            copyPropertiesFile(securityRealmValue.get(AUTHORIZATION, PROPERTIES), source, target, context);
        }
    }

    private void copyPropertiesFile(ModelNode properties, ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        if (properties.hasDefined(PATH)) {
            final String path = properties.get(PATH).asString();
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties path: %s", path);
            String relativeTo = null;
            if (properties.hasDefined(RELATIVE_TO)) {
                relativeTo = properties.get(RELATIVE_TO).asString();
            }
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties relative_to: %s", String.valueOf(relativeTo));
            final Path targetPath;
            if (relativeTo == null) {
                // path is absolute
                targetPath = Paths.get(path);
            } else {
                // path is relative to relative_to
                final Path resolvedPath = target.resolvePath(relativeTo);
                if (resolvedPath == null) {
                    throw new IOException("failed to resolve path "+relativeTo);
                } else {
                    targetPath = resolvedPath.normalize().resolve(path);
                }
            }
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties file path target: %s", targetPath);
            final Path targetServerBaseDir = target.getServer().getBaseDir();
            if (targetPath.startsWith(targetServerBaseDir)) {
                // properties file resolved to server's base dir, copy
                final Path sourcePath = source.getServer().getBaseDir().resolve(targetServerBaseDir.relativize(targetPath));
                context.getMigrationFiles().copy(sourcePath, targetPath);
            } else {
                // ignore, files not in base dir are not migrated
            }
        }
    }
}