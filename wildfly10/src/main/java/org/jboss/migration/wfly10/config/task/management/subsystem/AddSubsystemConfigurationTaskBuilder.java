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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;

/**
 * The builder for leaf tasks, which add subsystem configs.
 * @author emmartins
 */
public class AddSubsystemConfigurationTaskBuilder<S> extends ManageableResourceLeafTask.Builder<S, SubsystemConfiguration.Parent> {

    private String subsystem;

    public AddSubsystemConfigurationTaskBuilder(String subsystem) {
        this.subsystem = subsystem;
        name(parameters -> new ServerMigrationTaskName.Builder("add-subsystem-config").addAttribute("name", parameters.getResource().getSubsystemConfigurationAbsoluteName(subsystem)).build());
        run((params, taskName) -> taskContext -> {
            SubsystemConfiguration.Parent parent = params.getResource();
            if (parent.getSubsystemConfiguration(subsystem) != null) {
                taskContext.getLogger().infof("Skipped adding subsystem config %s, already exists.", parent.getSubsystemConfigurationAbsoluteName(subsystem));
                return ServerMigrationTaskResult.SKIPPED;
            }
            final String configName = parent.getSubsystemConfigurationAbsoluteName(subsystem);
            taskContext.getLogger().debugf("Adding subsystem config %s...", configName);
            addConfiguration(params, taskName, taskContext);
            taskContext.getLogger().infof("Subsystem config %s added.", configName);
            return ServerMigrationTaskResult.SUCCESS;
        });
    }

    protected void addConfiguration(ManageableResourceBuildParameters<S, SubsystemConfiguration.Parent> params, ServerMigrationTaskName taskName, TaskContext taskContext) {
        final ModelNode op = Util.createAddOperation(params.getResource().getSubsystemConfigurationPathAddress(subsystem));
        params.getServerConfiguration().executeManagementOperation(op);
    }

    public String getSubsystem() {
        return subsystem;
    }
}
