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
import org.jboss.migration.core.console.BasicResultHandlers;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfigurationPath;
import org.jboss.migration.core.jboss.ResolvablePath;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ServerGroupResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationBuildParameters;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.NO;
import static org.jboss.migration.core.console.BasicResultHandlers.UserConfirmation.Result.YES;
import static org.jboss.migration.wfly10.config.management.ManageableResourceSelectors.selectResources;

/**
 * Task which handles the migration/removal of a server configuration's persisted deployments.
 * @author emmartins
 */
public class MigratePersistentDeployments<S extends JBossServer<S>> extends ManageableServerConfigurationCompositeTask.Builder<JBossServerConfigurationPath<S>> {

    public MigratePersistentDeployments() {
        name("deployments.migrate-persistent-deployments");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        runBuilder(params -> context -> {
            context.getLogger().infof("Retrieving the configuration's persistent deployments...");
            // FIXME only deployment resources which are direct children of the server config
            final List<DeploymentResource> deploymentResources = params.getServerConfiguration().getChildResources(DeploymentResource.RESOURCE_TYPE);
            if (deploymentResources.isEmpty()) {
                context.getLogger().infof("No persistent deployments found.");
                return ServerMigrationTaskResult.SKIPPED;
            } else {
                context.getLogger().infof("Persistent deployments found: %s", deploymentResources.stream().map(resource -> resource.getResourceName()).collect(toList()));
                // find out if all deployments should be migrated
                final boolean confirmEachResource;
                if (context.isInteractive()) {
                    if (deploymentResources.size() > 1) {
                        final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                        new UserConfirmation(context.getConsoleWrapper(), "Migrate all persisted deployments?","yes/no?", userConfirmation).execute();
                        confirmEachResource = userConfirmation.getResult() == NO;
                    } else {
                        confirmEachResource = true;
                    }
                } else {
                    confirmEachResource = false;
                }
                // execute subtasks
                for (DeploymentResource deploymentResource : deploymentResources) {
                    final boolean migrateDeployment;
                    if (confirmEachResource) {
                        final BasicResultHandlers.UserConfirmation userConfirmation = new BasicResultHandlers.UserConfirmation();
                        new UserConfirmation(context.getConsoleWrapper(), "Migrate persisted deployment named '"+deploymentResource.getResourceName()+"'?","yes/no?", userConfirmation).execute();
                        migrateDeployment = userConfirmation.getResult() == YES;
                    } else {
                        // TODO add env property for a config on this decision
                        migrateDeployment = true;
                    }
                    final ManageableServerConfigurationLeafTask.Builder<JBossServerConfigurationPath<S>> subtaskBuilder = migrateDeployment ? new MigrateDeploymentSubtask<>(deploymentResource) : new RemoveDeploymentSubtask<>(deploymentResource);
                    context.execute(subtaskBuilder.build(params));
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        });
    }

    public static class MigrateDeploymentSubtask<S extends JBossServer<S>> extends ManageableServerConfigurationLeafTask.Builder<JBossServerConfigurationPath<S>> {
        protected MigrateDeploymentSubtask(DeploymentResource resource) {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("deployments.migrate-persistent-deployment").addAttribute("resource", resource.getResourceAbsoluteName()).build());
            final TaskRunnable.Builder<ManageableServerConfigurationBuildParameters<JBossServerConfigurationPath<S>>> runnableBuilder = params -> context -> {
                final ModelNode deploymentConfig = resource.getResourceConfiguration();
                if (!deploymentConfig.hasDefined(CONTENT)) {
                    throw new ServerMigrationFailureException("Unexpected persistent deployment "+resource.getResourceName()+" configuration: "+deploymentConfig.asString());
                }
                for (ModelNode content : deploymentConfig.get(CONTENT).asList()) {
                    if (content.hasDefined(HASH)) {
                        new MigrateContent(content, params.getSource(), params.getServerConfiguration()).run(context);
                    } else if (content.hasDefined(PATH)) {
                        new MigrateResolvablePath(new ResolvablePath(content), params.getSource(), params.getServerConfiguration()).run(context);
                    } else {
                        throw new ServerMigrationFailureException("Unexpected persistent deployment "+resource.getResourceName()+" content: "+content.asString());
                    }
                }
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }

    public static class RemoveDeploymentSubtask<S extends JBossServer<S>> extends ManageableServerConfigurationLeafTask.Builder<JBossServerConfigurationPath<S>> {
        protected RemoveDeploymentSubtask(DeploymentResource resource) {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("deployments.remove-persistent-resource").addAttribute("resource", resource.getResourceAbsoluteName()).build());
            final TaskRunnable.Builder<ManageableServerConfigurationBuildParameters<JBossServerConfigurationPath<S>>> runnableBuilder = params -> context -> {
                if (params.getServerConfiguration() instanceof HostControllerConfiguration) {
                    // the deployment may be referenced in server groups, remove these first
                    for (DeploymentResource serverGroupDeploymentResource : selectResources(ServerGroupResource.class).andThen(selectResources(DeploymentResource.class, resource.getResourceName())).fromResources(params.getServerConfiguration())) {
                        serverGroupDeploymentResource.removeResource();
                        context.getLogger().infof("Removed server group persistent deployment configuration %s", resource.getResourceAbsoluteName());
                    }
                }
                resource.removeResource();
                context.getLogger().infof("Removed persistent deployment configuration %s", resource.getResourceAbsoluteName());
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}