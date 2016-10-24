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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.JBossServer;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.task.SecurityRealmsMigration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.AUTHENTICATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.AUTHORIZATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class MigrateCompatibleSecurityRealm<S extends JBossServer<S>> implements SecurityRealmsMigration.SubtaskFactory<ServerPath<S>> {

    public static final String SECURITY_REALM = "security-realm";

    @Override
    public void addSubtasks(ServerPath<S> source, SecurityRealmsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, final ServerPath<S> source, final SecurityRealmsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder(SECURITY_REALM).addAttribute("name", resourceName).build();
        subtasks.add(new Task<>(taskName, source, resourceName, resourceManagement));
    }

    protected static class Task<S extends Server> implements ServerMigrationTask {

        private final ServerMigrationTaskName name;
        private final ServerPath<S> source;
        private final String securityRealmName;
        private final SecurityRealmsManagement securityRealmsManagement;

        protected Task(ServerMigrationTaskName name, ServerPath<S> source, String securityRealmName, SecurityRealmsManagement securityRealmsManagement) {
            this.name = name;
            this.source = source;
            this.securityRealmName = securityRealmName;
            this.securityRealmsManagement = securityRealmsManagement;
        }

        @Override
        public ServerMigrationTaskName getName() {
            return name;
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            context.getLogger().debugf("Security realm %s migration starting...", securityRealmName);
            final ModelNode securityRealmConfig = securityRealmsManagement.getResource(securityRealmName);
            if (securityRealmConfig.hasDefined(AUTHENTICATION, PROPERTIES)) {
                copyPropertiesFile(AUTHENTICATION, securityRealmName, securityRealmConfig, source, securityRealmsManagement, context);
            }
            if (securityRealmConfig.hasDefined(AUTHORIZATION, PROPERTIES)) {
                copyPropertiesFile(AUTHORIZATION, securityRealmName, securityRealmConfig, source, securityRealmsManagement, context);
            }
            context.getLogger().infof("Security realm %s migrated.", securityRealmName);

            return ServerMigrationTaskResult.SUCCESS;
        }

        private void copyPropertiesFile(String propertiesName, String securityRealmName, ModelNode securityRealmConfig, ServerPath<S> source, SecurityRealmsManagement securityRealmsManagement, ServerMigrationTaskContext context) throws IOException {
            final Server sourceServer = source.getServer();
            final Server targetServer = securityRealmsManagement.getServerConfiguration().getServer();
            final ModelNode properties = securityRealmConfig.get(propertiesName);
            if (properties.hasDefined(PATH)) {
                final String path = properties.get(PATH).asString();
                context.getLogger().debugf("Properties path: %s", path);
                String relativeTo = null;
                if (properties.hasDefined(RELATIVE_TO)) {
                    relativeTo = properties.get(RELATIVE_TO).asString();
                }
                context.getLogger().debugf("Properties relative_to: %s", String.valueOf(relativeTo));
                if (relativeTo == null) {
                    // path is absolute, if in source server base dir then relativize and copy to target server base dir, and update the config with the new path
                    final Path sourcePath = Paths.get(path);
                    context.getLogger().infof("Source Properties file path: %s", sourcePath);
                    if (sourcePath.startsWith(sourceServer.getBaseDir())) {
                        final Path targetPath = sourceServer.getBaseDir().resolve(targetServer.getBaseDir().relativize(sourcePath));
                        context.getLogger().infof("Target Properties file path: %s", targetPath);
                        context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                        final PathAddress pathAddress = securityRealmsManagement.getResourcePathAddress(securityRealmName).append(PathElement.pathElement(propertiesName, PROPERTIES));
                        final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                        op.get(NAME).set(PATH);
                        op.get(VALUE).set(targetPath.toString());
                        securityRealmsManagement.getServerConfiguration().executeManagementOperation(op);
                    } else {
                        context.getLogger().infof("Source Properties file path is not in source server base dir, skipping file copy");
                    }
                } else {
                    // path is relative to relative_to
                    final Path resolvedSourcePath = sourceServer.resolvePath(relativeTo);
                    if (resolvedSourcePath == null) {
                        throw new IOException("failed to resolve source path "+relativeTo);
                    }
                    final Path sourcePath = resolvedSourcePath.normalize().resolve(path);
                    context.getLogger().infof("Source Properties file path: %s", sourcePath);
                    final Path resolvedTargetPath = targetServer.resolvePath(relativeTo);
                    if (resolvedTargetPath == null) {
                        throw new IOException("failed to resolve target path "+relativeTo);
                    }
                    final Path targetPath = resolvedTargetPath.normalize().resolve(path);
                    context.getLogger().infof("Target Properties file path: %s", targetPath);
                    if (!sourcePath.equals(targetPath)) {
                        context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                    } else {
                        context.getLogger().infof("Resolved paths for Source and Target Properties files is the same.");
                    }
                }
            }
        }
    }

    public SecurityRealmsMigration<ServerPath<S>> buildSecurityRealmsMigration() {
        return new SecurityRealmsMigration.Builder<ServerPath<S>>().subtask(this).build();
    }
}