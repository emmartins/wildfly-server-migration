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

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.wfly10.config.management.DeploymentsManagement;
import org.jboss.migration.wfly10.config.task.DeploymentsMigration;

/**
 * No support for deployment migration yet so each deploymentis removed from the config.
 * @author emmartins
 */
public class RemoveDeployment<S> implements DeploymentsMigration.SubtaskFactory<S> {

    public static final String REMOVE_DEPLOYMENT = "remove-deployment";

    @Override
    public void addSubtasks(S source, DeploymentsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, final S source, final DeploymentsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder(REMOVE_DEPLOYMENT).addAttribute("name", resourceName).build();
        subtasks.add(new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return subtaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                resourceManagement.removeResource(resourceName);
                context.getLogger().infof("Removed deployment %s", resourceName);
                return ServerMigrationTaskResult.SUCCESS;
            }
        });
    }

    public DeploymentsMigration<S> buildDeploymentsMigration() {
        return new DeploymentsMigration.Builder<S>().subtask(this).build();
    }
}