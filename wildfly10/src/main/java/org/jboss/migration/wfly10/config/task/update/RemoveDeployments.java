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

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;

/**
 * Removes deployments from configs.
 * @author emmartins
 */
public class RemoveDeployments<S> extends ServerConfigurationCompositeTask.Builder<S> {

    private RemoveDeployments() {
        name("remove-deployments");
        beforeRun(context -> context.getLogger().infof("Deployments removal starting..."));
        subtasks(DeploymentResource.class, ResourceCompositeSubtasks.of(new Subtask<>()));
        afterRun(context -> context.getLogger().infof("Deployments removal done."));
    }

    public static class Subtask<S> extends ResourceLeafTask.Builder<S, DeploymentResource> {
        protected Subtask() {
            name(parameters -> new ServerMigrationTaskName.Builder("remove-deployment").addAttribute("name", parameters.getResource().getResourceName()).build());
            final ResourceTaskRunnableBuilder<S, DeploymentResource> runnableBuilder = (params, taskName) -> context -> {
                final DeploymentResource resource = params.getResource();
                final String resourceName = resource.getResourceName();
                resource.remove();
                context.getLogger().infof("Removed deployment %s", resourceName);
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(runnableBuilder);
        }
    }
}