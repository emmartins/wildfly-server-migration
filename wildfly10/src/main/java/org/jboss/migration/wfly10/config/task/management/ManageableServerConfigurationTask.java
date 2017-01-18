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
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ExtensionsManagementSubtaskExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationTask<S, T extends ManageableServerConfiguration> extends ParentTask {

    private static final Subtasks[] EMPTY = {};
    protected final Subtasks<S, T>[] subtasks;
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
        for (Subtasks<S, T> subtask : subtasks) {
            subtask.run(source, configuration, context);
        }
    }

    public interface Subtasks<S, T extends ManageableServerConfiguration> {
        void run(S source, T configuration, TaskContext taskContext) throws Exception;
    }

    protected abstract static class BaseBuilder<S, C extends ManageableServerConfiguration, T extends Subtasks<S, C>, B extends BaseBuilder<S, C, T, B>> extends ParentTask.BaseBuilder<B> {

        protected final List<Subtasks<S, C>> subtasks;

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
            this.subtasks = new ArrayList<>();
        }

        @Override
        public B subtask(ServerMigrationTask subtask) {
            return subtask((Subtasks<S, C>) (source, configuration, context) -> subtask.run(context));
        }

        @Override
        public B subtask(ParentTask.Subtasks subtasks) {
            return subtask((Subtasks<S, C>) (source, configuration, context) -> subtasks.run(context));
        }

        public B subtask(Subtasks<S, C> subtasks) {
            this.subtasks.add(subtasks);
            return (B) this;
        }

        public B subtask(BaseBuilder<S, C, ?, ?> taskBuilder) {
            return subtask((Subtasks<S, C>) (source, configuration, context) -> {
                final ServerMigrationTask subtask = taskBuilder.build(source, configuration);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public <R extends ManageableResources> B subtask(Class<R> childrenType, ManageableResourcesTask.BaseBuilder<S, R, ?, ?> taskBuilder) {
            return subtask((Subtasks<S, C>) (source, configuration, context) -> {
                final List<R> children = configuration.findResourcesByType(childrenType);
                if (!children.isEmpty()) {
                    final ServerMigrationTask subtask = taskBuilder.build(source, children);
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
            });
        }

        public B subtask(ExtensionsManagementSubtaskExecutor<S> subtask) {
            return subtask((Subtasks<S, C>) (source, configuration, taskContext) -> subtask.executeSubtasks(source, configuration.getExtensionsManagement(), taskContext));
        }

        public abstract ServerMigrationTask build(S source, C configuration);
    }

    public static class Builder<S, C extends ManageableServerConfiguration> extends BaseBuilder<S, C, Subtasks<S, C>, Builder<S, C>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, C configuration) {
            return new ManageableServerConfigurationTask<>(this, source, configuration);
        }
    }
}
