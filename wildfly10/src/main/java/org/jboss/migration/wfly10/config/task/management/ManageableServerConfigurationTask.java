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

import org.jboss.migration.core.ParentTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ExtensionsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationTask<S, T extends ManageableServerConfiguration> extends ParentTask {

    private static final ManageableServerConfigurationSubtaskExecutor[] EMPTY = {};
    protected final ManageableServerConfigurationSubtaskExecutor<S, T>[] subtasks;
    protected final S source;
    protected final T configuration;

    protected ManageableServerConfigurationTask(BaseBuilder<S, T, ?, ?> builder, S source, T configuration) {
        super(builder);
        this.subtasks = builder.subtasks.toArray(EMPTY);
        this.source = source;
        this.configuration = configuration;
    }

    @Override
    protected void runSubtasks(TaskContext context) throws Exception {
        for (ManageableServerConfigurationSubtaskExecutor<S, T> subtaskExecutor : subtasks) {
            subtaskExecutor.run(source, configuration, context);
        }
    }

    protected abstract static class BaseBuilder<S, C extends ManageableServerConfiguration, T extends ManageableServerConfigurationSubtaskExecutor<S, C>, B extends BaseBuilder<S, C, T, B>> extends ParentTask.BaseBuilder<B> {

        protected final List<ManageableServerConfigurationSubtaskExecutor<S, C>> subtasks;

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
            this.subtasks = new ArrayList<>();
        }

        @Override
        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new ManageableServerConfigurationSubtaskExecutor<S, C>() {
                @Override
                public void run(S source, C configuration, TaskContext context) throws Exception {
                    subtask.run(context);
                }
            });
        }

        @Override
        public B subtask(final Subtasks subtasks) {
            return subtask(new ManageableServerConfigurationSubtaskExecutor<S, C>() {
                @Override
                public void run(S source, C configuration, TaskContext context) throws Exception {
                    subtasks.run(context);
                }
            });
        }

        public B subtask(ManageableServerConfigurationSubtaskExecutor<S, C> subtaskExecutor) {
            subtasks.add(subtaskExecutor);
            return (B) this;
        }

        public B subtask(final BaseBuilder<S, C, ?, ?> taskBuilder) {
            return subtask(new ManageableServerConfigurationSubtaskExecutor<S, C>() {
                @Override
                public void run(S source, C configuration, TaskContext context) throws Exception {
                    final ServerMigrationTask subtask = taskBuilder.build(source, configuration);
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
            });
        }

        public B subtask(final ExtensionsManagementSubtaskExecutor<S> subtask) {
            return subtask(new ManageableServerConfigurationSubtaskExecutor<S, C>() {
                @Override
                public void run(S source, C configuration, TaskContext taskContext) throws Exception {
                    subtask.executeSubtasks(source, configuration.getExtensionsManagement(), taskContext);
                }
            });
        }

        public abstract ServerMigrationTask build(S source, C configuration);
    }

    public static class Builder<S, C extends ManageableServerConfiguration> extends BaseBuilder<S, C, ManageableServerConfigurationSubtaskExecutor<S, C>, Builder<S, C>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, C configuration) {
            return new ManageableServerConfigurationTask<>(this, source, configuration);
        }
    }
}
