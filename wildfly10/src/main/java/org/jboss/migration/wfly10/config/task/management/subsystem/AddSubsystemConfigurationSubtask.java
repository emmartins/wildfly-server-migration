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
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

/**
 * A task which creates a subsystem if its missing from the server's config.
 * @author emmartins
 */
public class AddSubsystemConfigurationSubtask<S> implements SubsystemConfigurationParentCompositeTask.SubtaskFactory<S> {

    protected final String subsystem;

    public AddSubsystemConfigurationSubtask(String subsystem) {
        this.subsystem = subsystem;
    }

    @Override
    public ServerMigrationTask getTask(S source, SubsystemConfiguration.Parent parent, TaskContext context) throws Exception {
        final ServerMigrationTaskName taskName = getName(source, parent, context);
        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem, taskName.getName()));
        final SubsystemConfigurationParentLeafTask.Runnable<S> runnable = (source1, resource1, context1) -> AddSubsystemConfigurationSubtask.this.run(source1, resource1, context1, taskEnvironment);
        return new SubsystemConfigurationParentLeafTask.Builder<>(taskName, runnable)
                .skipper(context1 -> taskEnvironment.isSkippedByEnvironment())
                .build(source, parent);
    }

    protected ServerMigrationTaskName getName(S source, SubsystemConfiguration.Parent resource, TaskContext context) {
        return new ServerMigrationTaskName.Builder("add-subsystem-config").addAttribute("name", resource.getSubsystemConfigurationAbsoluteName(subsystem)).build();
    }

    protected ServerMigrationTaskResult run(S source, SubsystemConfiguration.Parent parent, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        if (parent.getSubsystemConfiguration(subsystem) != null) {
            taskContext.getLogger().infof("Skipped adding subsystem config %s, already exists.", parent.getSubsystemConfigurationAbsoluteName(subsystem));
            return ServerMigrationTaskResult.SKIPPED;
        }
        final String configName = parent.getSubsystemConfigurationAbsoluteName(subsystem);
        taskContext.getLogger().debugf("Adding subsystem config %s...", configName);
        addConfiguration(source, parent, taskContext, taskEnvironment);
        taskContext.getLogger().infof("Subsystem config %s added.", configName);
        return ServerMigrationTaskResult.SUCCESS;
    }

    protected void addConfiguration(S source, SubsystemConfiguration.Parent parent, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final ModelNode op = Util.createAddOperation(parent.getSubsystemConfigurationPathAddress(subsystem));
        parent.getServerConfiguration().executeManagementOperation(op);
    }
}
