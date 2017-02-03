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
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class MigrateCompatibleSecurityRealms<S extends JBossServer<S>> extends ServerConfigurationCompositeTask.Builder<ServerPath<S>> {

    public MigrateCompatibleSecurityRealms() {
        name("migrate-compatible-security-realms");
        beforeRun(context -> context.getLogger().infof("Migrating security realms..."));
        subtasks(SecurityRealmResource.class, ResourceCompositeSubtasks.of(new Subtask<>()));
        afterRun(context -> context.getLogger().infof("Security realms migration done."));

    }

    protected static class Subtask<S extends JBossServer<S>> extends ResourceLeafTask.Builder<ServerPath<S>, SecurityRealmResource> {
        protected Subtask() {
            name(parameters -> new ServerMigrationTaskName.Builder("migrate-compatible-security-realm").addAttribute("name", parameters.getResource().getResourceName()).build());
            beforeRun((params, taskName) -> context -> context.getLogger().debugf("Security realm %s migration starting...", params.getResource().getResourceName()));
            afterRun((params, taskName) -> context -> context.getLogger().infof("Security realm %s migrated.", params.getResource().getResourceName()));
            final ResourceTaskRunnableBuilder<ServerPath<S>, SecurityRealmResource> runnableBuilder = (params, taskName) -> context -> {
                final SecurityRealmResource securityRealmResource = params.getResource();
                final ModelNode securityRealmConfig = securityRealmResource.getResourceConfiguration();
                if (securityRealmConfig.hasDefined(AUTHENTICATION, PROPERTIES)) {
                    copyPropertiesFile(AUTHENTICATION, securityRealmConfig, params.getSource(), securityRealmResource, context);
                }
                if (securityRealmConfig.hasDefined(AUTHORIZATION, PROPERTIES)) {
                    copyPropertiesFile(AUTHORIZATION, securityRealmConfig, params.getSource(), securityRealmResource, context);
                }
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(runnableBuilder);
        }

        private void copyPropertiesFile(String propertiesName, ModelNode securityRealmConfig, ServerPath<S> source, SecurityRealmResource securityRealmResource, TaskContext context) throws ServerMigrationFailureException {
            final Server sourceServer = source.getServer();
            final Server targetServer = securityRealmResource.getServerConfiguration().getServer();
            final ModelNode properties = securityRealmConfig.get(propertiesName, PROPERTIES);
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
                    context.getLogger().debugf("Source Properties file path: %s", sourcePath);
                    if (sourcePath.startsWith(sourceServer.getBaseDir())) {
                        final Path targetPath = sourceServer.getBaseDir().resolve(targetServer.getBaseDir().relativize(sourcePath));
                        context.getLogger().debugf("Target Properties file path: %s", targetPath);
                        context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                        final PathAddress pathAddress = securityRealmResource.getResourcePathAddress().append(PathElement.pathElement(propertiesName, PROPERTIES));
                        final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                        op.get(NAME).set(PATH);
                        op.get(VALUE).set(targetPath.toString());
                        securityRealmResource.getServerConfiguration().executeManagementOperation(op);
                    } else {
                        context.getLogger().debugf("Source Properties file path is not in source server base dir, skipping file copy");
                    }
                } else {
                    // path is relative to relative_to
                    final Path resolvedSourcePath = sourceServer.resolvePath(relativeTo);
                    if (resolvedSourcePath == null) {
                        throw new ServerMigrationFailureException("failed to resolve source path "+relativeTo);
                    }
                    final Path sourcePath = resolvedSourcePath.normalize().resolve(path);
                    context.getLogger().debugf("Source Properties file path: %s", sourcePath);
                    final Path resolvedTargetPath = targetServer.resolvePath(relativeTo);
                    if (resolvedTargetPath == null) {
                        throw new ServerMigrationFailureException("failed to resolve target path "+relativeTo);
                    }
                    final Path targetPath = resolvedTargetPath.normalize().resolve(path);
                    context.getLogger().debugf("Target Properties file path: %s", targetPath);
                    if (!sourcePath.equals(targetPath)) {
                        context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                    } else {
                        context.getLogger().debugf("Resolved paths for Source and Target Properties files is the same.");
                    }
                }
            }
        }
    }
}