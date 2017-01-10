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
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;

import java.util.List;

/**
 * @author emmartins
 */
public class StandaloneServerConfigurationTask<S> extends ManageableServerConfigurationTask<S, StandaloneServerConfiguration> {

    protected StandaloneServerConfigurationTask(Builder<S> builder, List<ParentTask.Subtasks> subtasks) {
        super(builder, subtasks);
    }

    public interface Subtasks<S> extends ManageableServerConfigurationTask.Subtasks<S, StandaloneServerConfiguration> {
    }

    public static class Builder<S> extends ManageableServerConfigurationTask.BaseBuilder<S, StandaloneServerConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ServerMigrationTask build(List<ParentTask.Subtasks> subtasks) {
            return new StandaloneServerConfigurationTask<>(this, subtasks);
        }

        public Builder<S> subtask(final SubsystemsManagementSubtaskExecutor<S> subtask) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, StandaloneServerConfiguration configuration, TaskContext taskContext) throws Exception {
                    subtask.executeSubtasks(source, configuration.getSubsystemsManagement(), taskContext);
                }
            });
        }
    }
}
