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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationTask;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationSubtasks;

/**
 * @author emmartins
 */
public class HostConfigurationTask<S> extends ManageableServerConfigurationTask<S, HostConfiguration> {

    protected HostConfigurationTask(Builder<S> builder, S source, HostConfiguration configuration) {
        super(builder, source, configuration);
    }

    public interface Subtasks<S> extends ManageableServerConfigurationSubtaskExecutor<S, HostConfiguration> {
    }

    public static class Builder<S> extends ManageableServerConfigurationTask.BaseBuilder<S, HostConfiguration, Subtasks<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public Builder<S> subtask(final SubsystemsConfigurationSubtasks<S> subtask) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostConfiguration configuration, TaskContext taskContext) throws Exception {
                    subtask.executeSubtasks(source, configuration.getSubsystemsManagement(), taskContext);
                }
            });
        }

        public Builder<S> subtask(final SubsystemsConfigurationTask.Builder<S> subtaskBuilder) {
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

        @Override
        public ServerMigrationTask build(S source, HostConfiguration configuration) {
            return new HostConfigurationTask<>(this, source, configuration);
        }
    }
}
