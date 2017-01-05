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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.task.factory.SubsystemManagementParentTask;

/**
 * @author emmartins
 */
public abstract class AbstractSubsystemSubtask<S> implements SubsystemManagementParentTask.SubtaskExecutor<S> {

    @Override
    public void run(final SubsystemManagementParentTask.Context<S> parentContext) throws Exception {
        final ServerMigrationTaskName taskName = getTaskName(parentContext);
        final TaskEnvironment taskEnvironment = new TaskEnvironment(parentContext.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(parentContext.getSubsystem(), taskName.getName()));
        final AbstractServerMigrationTask.Builder builder = new AbstractServerMigrationTask.Builder(taskName)
                .skipper(new AbstractServerMigrationTask.Skipper() {
                    @Override
                    public boolean isSkipped(TaskContext context) {
                        return taskEnvironment.isSkippedByEnvironment();
                    }
                });
        final ServerMigrationTask subtask = new AbstractServerMigrationTask(builder) {
            @Override
            protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
                return AbstractSubsystemSubtask.this.runTask(parentContext, taskContext, taskEnvironment);
            }
        };
        parentContext.execute(subtask);
    }

    protected abstract ServerMigrationTaskName getTaskName(SubsystemManagementParentTask.Context<S> parentTaskContext);

    protected abstract ServerMigrationTaskResult runTask(SubsystemManagementParentTask.Context<S> parentTaskContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception;
}
