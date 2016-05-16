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
package org.jboss.migration.wfly10.standalone.config;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSecurityRealmsMigration<S extends Server> {

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("Security Realms").build();

    public static final String SERVER_MIGRATION_TASK_SECURITY_REALM_NAME = "Security Realm: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHENTICATION_PROPERTIES_SOURCE = "Authentication Properties Source: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHENTICATION_PROPERTIES_TARGET = "Authentication Properties Target: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHORIZATION_PROPERTIES_SOURCE = "Authorization Properties Source: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHORIZATION_PROPERTIES_TARGET = "Authorization Properties Target: ";

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> source, final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                WildFly10StandaloneConfigFileSecurityRealmsMigration.this.run(source, target, context);
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    protected void run(ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationTaskContext context) throws IOException {
        /*if (context.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() {
                    ServerMigrationLogger.ROOT_LOGGER.info("Security realms migration skipped by user.");
                }
                @Override
                public void onYes() {
                    try {
                        migrateSecurityRealms(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onError() {
                    // repeat
                    try {
                        run(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
            };
            new UserConfirmation(context.getConsoleWrapper(), "Migrate security realms?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
        */
        // by default security realms are migrated
        migrateSecurityRealms(source, target, context);
        //}
    }

    protected void migrateSecurityRealms(ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationTaskContext context) throws IOException {
        context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
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

    protected void migrateSecurityRealm(final ModelNode securityRealm, final ServerPath<S> source, final WildFly10StandaloneServer target, final ServerMigrationTaskContext context) throws IOException {
        final Property securityRealmProperty = securityRealm.asProperty();
        final String securityRealmName = securityRealmProperty.getName();
        final ServerMigrationTaskId securityRealmMigrationTaskId = new ServerMigrationTaskId.Builder().setName(SERVER_MIGRATION_TASK_SECURITY_REALM_NAME).addAttribute("name", securityRealmName).build();
        final ServerMigrationTask securityRealmMigrationTask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return securityRealmMigrationTaskId;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                ServerMigrationLogger.ROOT_LOGGER.infof("Migrating security realm: %s", securityRealmName);
                final ModelNode securityRealmValue = securityRealmProperty.getValue();
                if (securityRealmValue.hasDefined(AUTHENTICATION, PROPERTIES)) {
                    copyPropertiesFile(securityRealmValue.get(AUTHENTICATION, PROPERTIES), source, target, context);
                }
                if (securityRealmValue.hasDefined(AUTHORIZATION, PROPERTIES)) {
                    copyPropertiesFile(securityRealmValue.get(AUTHORIZATION, PROPERTIES), source, target, context);
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        context.execute(securityRealmMigrationTask);
    }

    private void copyPropertiesFile(ModelNode properties, ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationTaskContext context) throws IOException {
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
                context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                //reportTask.getAttributes().put(reportTaskAttrSource, sourcePath.toString());
                //reportTask.getAttributes().put(reportTaskAttrTarget, targetPath.toString());
            } else {
                // ignore, files not in base dir are not migrated
            }
        }
    }
}