/*
 * Copyright 2017 Red Hat, Inc.
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

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.console.BasicResultHandlers;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.jboss.ContentPathMapper;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ServerGroupResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationBuildParameters;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.NO;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.YES;
import static org.jboss.migration.wfly10.config.management.ManageableResourceSelectors.selectResources;

/**
 * Task which handles the migration/removal of a server configuration's persisted deployments.
 * @author emmartins
 */
public class MigrateDeployments<S extends JBossServer<S>> extends ManageableServerConfigurationCompositeTask.Builder<ServerPath<S>> {

    public MigrateDeployments() {
        name("deployments.migrate-persistent-deployments");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        runBuilder(params -> context -> {
            context.getLogger().infof("Retrieving the configuration's persistent deployments...");
            // FIXME only deployment resources which are direct children of the server config
            final List<DeploymentResource> deploymentResources = params.getServerConfiguration().getChildResources(DeploymentResource.RESOURCE_TYPE);
            if (deploymentResources.isEmpty()) {
                context.getLogger().infof("No deployments found.");
                return ServerMigrationTaskResult.SKIPPED;
            } else {
                context.getLogger().infof("Deployments found: %s", deploymentResources.stream().map(resource -> resource.getResourceName()));
                // find out if all deployments should be migrated
                final boolean confirmEachDeployment;
                if (context.isInteractive()) {
                    final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                    new UserConfirmation(context.getConsoleWrapper(), "Migrate all deployments?","yes/no?", userConfirmation).execute();
                    confirmEachDeployment = userConfirmation.getResult() == NO;
                } else {
                    confirmEachDeployment = false;
                }
                // execute subtasks
                for (DeploymentResource deploymentResource : deploymentResources) {
                    final boolean migrateDeployment;
                    if (confirmEachDeployment) {
                        final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                        new UserConfirmation(context.getConsoleWrapper(), "Migrate deployment named '"+deploymentResource.getResourceName()+"'?","yes/no?", userConfirmation).execute();
                        migrateDeployment = userConfirmation.getResult() == YES;
                    } else {
                        // TODO add env property for a config on this decision
                        migrateDeployment = true;
                    }
                    final ManageableServerConfigurationLeafTask.Builder<ServerPath<S>> subtaskBuilder = migrateDeployment ? new MigrateDeploymentSubtask<>(deploymentResource) : new RemoveDeploymentSubtask<>(deploymentResource);
                    context.execute(subtaskBuilder.build(params));
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        });
    }

    public static class MigrateDeploymentSubtask<S extends JBossServer<S>> extends ManageableServerConfigurationLeafTask.Builder<ServerPath<S>> {
        protected MigrateDeploymentSubtask(DeploymentResource resource) {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("deployment."+resource.getResourceName()+".migrate").build());
            final TaskRunnable.Builder<ManageableServerConfigurationBuildParameters<ServerPath<S>>> runnableBuilder = params -> context -> {
                final ModelNode deploymentConfig = resource.getResourceConfiguration();
                if (!deploymentConfig.hasDefined(CONTENT)) {
                    throw new ServerMigrationFailureException("Unexpected deployment "+resource.getResourceName()+" configuration: "+deploymentConfig.asString());
                }
                final JBossServer sourceServer  = params.getSource().getServer();
                final JBossServer targetServer = params.getServerConfiguration().getServer();
                for (ModelNode content : deploymentConfig.get(CONTENT).asList()) {
                    if (content.hasDefined(HASH)) {
                        final byte[] contentHash = content.get(HASH).asBytes();
                        final Path contentPath = new ContentPathMapper().apply(contentHash);
                        final boolean standaloneConfig = params.getServerConfiguration() instanceof StandaloneServerConfiguration;
                        final Path sourceContentDir = standaloneConfig ? sourceServer.getStandaloneContentDir() : sourceServer.getDomainContentDir();
                        final Path contentSource = sourceContentDir.resolve(contentPath);
                        context.getLogger().infof("Source deployment content's path: %s", contentSource);
                        final Path targetContentDir = standaloneConfig ? targetServer.getStandaloneContentDir() : targetServer.getDomainContentDir();
                        final Path contentTarget = targetContentDir.resolve(contentPath);
                        context.getLogger().infof("Target deployment content's path: %s", contentTarget);
                        context.getMigrationFiles().copy(contentSource, contentTarget);
                        context.getLogger().infof("Deployment %s content %s migrated.", resource.getResourceAbsoluteName(), content.get(HASH).asString());
                    } else if (content.hasDefined(PATH)) {
                        final Path path = Paths.get(content.get(PATH).asString());
                        final String relativeTo = content.hasDefined(RELATIVE_TO) ? content.get(RELATIVE_TO).asString() : null;
                        if (relativeTo != null) {
                            Path sourceRelativeTo = sourceServer.resolvePath(relativeTo);
                            if (sourceRelativeTo == null) {
                                // give a try to the running server config, may have the path configured there
                                final Path targetRelativeTo = params.getServerConfiguration().resolvePath(relativeTo);
                                if (targetRelativeTo != null) {
                                    if (targetRelativeTo.startsWith(targetServer.getBaseDir())) {
                                        sourceRelativeTo = sourceServer.getBaseDir().resolve(targetServer.getBaseDir().relativize(targetRelativeTo));
                                    } else {
                                        sourceRelativeTo = targetRelativeTo;
                                    }
                                }
                                if (sourceRelativeTo == null || !Files.isDirectory(sourceRelativeTo)) {
                                    throw new ServerMigrationFailureException("Source server failed to resolve 'relative to' path "+relativeTo);
                                }
                            }
                            final Path contentSource = sourceRelativeTo.resolve(path);
                            context.getLogger().infof("Source deployment content's path: %s", contentSource);
                            Path targetRelativeTo = targetServer.resolvePath(relativeTo);
                            if (targetRelativeTo == null) {
                                targetRelativeTo = params.getServerConfiguration().resolvePath(relativeTo);
                                if (targetRelativeTo == null) {
                                    throw new ServerMigrationFailureException("Target server failed to resolve 'relative to' path "+relativeTo);
                                }
                            }
                            final Path contentTarget = targetRelativeTo.resolve(path);
                            context.getLogger().infof("Target deployment content's path: %s", contentTarget);
                            if (!contentSource.equals(contentTarget)) {
                                context.getMigrationFiles().copy(contentSource, contentTarget);
                            }
                        }
                        context.getLogger().infof("Deployment %s content %s migrated.", resource.getResourceAbsoluteName(), path);
                    } else {
                        throw new ServerMigrationFailureException("Unexpected deployment "+resource.getResourceName()+" content: "+content.asString());
                    }
                }
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }

    public static class RemoveDeploymentSubtask<S extends JBossServer<S>> extends ManageableServerConfigurationLeafTask.Builder<ServerPath<S>> {
        protected RemoveDeploymentSubtask(DeploymentResource resource) {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("deployment."+resource.getResourceName()+".remove").build());
            final TaskRunnable.Builder<ManageableServerConfigurationBuildParameters<ServerPath<S>>> runnableBuilder = params -> context -> {
                if (params.getServerConfiguration() instanceof HostControllerConfiguration) {
                    // the deployment may be referenced in server groups, remove these first
                    for (DeploymentResource serverGroupDeploymentResource : selectResources(ServerGroupResource.class).andThen(selectResources(DeploymentResource.class, resource.getResourceName())).fromResources(params.getServerConfiguration())) {
                        serverGroupDeploymentResource.removeResource();
                        context.getLogger().infof("Removed server group deployment configuration %s", resource.getResourceAbsoluteName());
                    }
                }
                resource.removeResource();
                context.getLogger().infof("Removed deployment configuration %s", resource.getResourceAbsoluteName());
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}