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

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemConfigurationTask;

/**
 * @author emmartins
 */
public abstract class UpdateSubsystemConfigurationSubtask<S> implements SubsystemConfigurationTask.Subtask<S> {

    @Override
    public ServerMigrationTaskName getName(SubsystemConfigurationTask.Context<S> parentContext) {
        return new ServerMigrationTaskName.Builder("update-subsystem-config").addAttribute("name", parentContext.getSubsystemConfigurationName()).build();
    }

    @Override
    public ServerMigrationTaskResult run(SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final String configName = parentContext.getSubsystemConfigurationName();
        final ModelNode config = parentContext.getSubsystemConfiguration();
        if (config == null) {
            taskContext.getLogger().infof("Skipped subsystem config %s update, not found.", configName);
            return ServerMigrationTaskResult.SKIPPED;
        }
        taskContext.getLogger().debugf("Updating subsystem config %s...", parentContext.getSubsystemConfigurationName());
        updateConfiguration(config, parentContext, taskContext, taskEnvironment);
        taskContext.getLogger().infof("Subsystem config %s updated.", configName);
        return ServerMigrationTaskResult.SUCCESS;
    }

    protected abstract void updateConfiguration(ModelNode config, SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception;
}
