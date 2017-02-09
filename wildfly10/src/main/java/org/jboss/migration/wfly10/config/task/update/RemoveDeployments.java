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
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

/**
 * Removes deployments from configs.
 * @author emmartins
 */
public class RemoveDeployments<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    public RemoveDeployments() {
        name("remove-deployments");
        beforeRun(context -> context.getLogger().infof("Deployments removal starting..."));
        subtasks(DeploymentResource.class, ManageableResourceCompositeSubtasks.of(new Subtask<>()));
        afterRun(context -> context.getLogger().infof("Deployments removal done."));
    }

    public static class Subtask<S> extends ManageableResourceLeafTask.Builder<S, DeploymentResource> {
        protected Subtask() {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("remove-deployment").addAttribute("resource", parameters.getResource().getResourceAbsoluteName()).build());
            final ManageableResourceTaskRunnableBuilder<S, DeploymentResource> runnableBuilder = params-> context -> {
                final DeploymentResource resource = params.getResource();
                resource.removeResource();
                context.getLogger().infof("Removed deployment configuration %s", resource.getResourceAbsoluteName());
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}