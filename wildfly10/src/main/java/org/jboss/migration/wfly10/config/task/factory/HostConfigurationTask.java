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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.migration.core.ParentTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;

import java.util.List;

/**
 * @author emmartins
 */
public class HostConfigurationTask<S> extends ManageableServerConfigurationTask<S, HostConfiguration> {

    protected HostConfigurationTask(Builder<S> builder, List<ParentTask.Subtasks> subtasks) {
        super(builder, subtasks);
    }

    public interface Subtasks<S> extends ManageableServerConfigurationTask.Subtasks<S, HostConfiguration> {
    }

    public static class Builder<S> extends BaseBuilder<S, HostConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ServerMigrationTask build(List<ParentTask.Subtasks> subtasks) {
            return new HostConfigurationTask<>(this, subtasks);
        }

        public Builder<S> subtask(final HostConfigurationTask.Builder<S> subtaskBuilder) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostConfiguration configuration, TaskContext taskContext) throws Exception {
                    final ServerMigrationTask subtask = subtaskBuilder.build(source, configuration);
                    if (subtask != null) {
                        taskContext.execute(subtask);
                    }
                }
            });
        }

        public Builder<S> subtask(final SubsystemsManagementSubtaskExecutor<S> subtask) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostConfiguration configuration, TaskContext taskContext) throws Exception {
                    subtask.executeSubtasks(source, configuration.getSubsystemsManagement(), taskContext);
                }
            });
        }

        public Builder<S> subtask(final SubsystemsManagementTask.Builder<S> subtaskBuilder) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostConfiguration configuration, TaskContext taskContext) throws Exception {
                    final ServerMigrationTask subtask = subtaskBuilder.build(source, configuration.getSubsystemsManagement());
                    if (subtask != null) {
                        taskContext.execute(subtask);
                    }
                }
            });
        }
    }
}
