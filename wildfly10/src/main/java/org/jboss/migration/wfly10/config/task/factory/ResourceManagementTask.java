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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ResourceManagement;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ResourceManagementSubtaskExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class ResourceManagementTask<S, R extends ResourceManagement> extends ParentTask {

    private static final ResourceManagementSubtaskExecutor[] EMPTY = {};
    protected final ResourceManagementSubtaskExecutor<S, R>[] subtasks;
    protected final S source;
    protected final R[] resourceManagements;

    protected ResourceManagementTask(BaseBuilder<S, R, ?, ?> builder, S source, R... resourceManagements) {
        super(builder);
        this.subtasks = builder.subtasks.toArray(EMPTY);
        this.source = source;
        this.resourceManagements = resourceManagements;
    }

    @Override
    protected void runSubtasks(TaskContext context) throws Exception {
        for (ResourceManagementSubtaskExecutor<S, R> subtaskExecutor : subtasks) {
            for (R resourceManagement : resourceManagements) {
                subtaskExecutor.executeSubtasks(source, resourceManagement, context);
            }
        }
    }

    protected abstract static class BaseBuilder<S, R extends ResourceManagement, T extends ResourceManagementSubtaskExecutor<S, R>, B extends BaseBuilder<S, R, T, B>> extends ParentTask.BaseBuilder<B> {

        protected final List<ResourceManagementSubtaskExecutor<S, R>> subtasks;

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
            this.subtasks = new ArrayList<>();
        }

        @Override
        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new ResourceManagementSubtaskExecutor<S, R>() {
                @Override
                public void executeSubtasks(S source, R resourceManagement, TaskContext context) throws Exception {
                    subtask.run(context);
                }
            });
        }

        @Override
        public B subtask(final Subtasks subtasks) {
            return subtask(new ResourceManagementSubtaskExecutor<S, R>() {
                @Override
                public void executeSubtasks(S source, R resourceManagement, TaskContext context) throws Exception {
                    subtasks.run(context);
                }
            });
        }

        public B subtask(ResourceManagementSubtaskExecutor<S, R> subtaskExecutor) {
            subtasks.add(subtaskExecutor);
            return (B) this;
        }

        public B subtask(final ManageableServerConfigurationSubtaskExecutor<S, ManageableServerConfiguration> subtaskExecutor) {
            return subtask(new ResourceManagementSubtaskExecutor<S, R>() {
                @Override
                public void executeSubtasks(S source, R resourceManagement, TaskContext context) throws Exception {
                    subtaskExecutor.run(source, resourceManagement.getServerConfiguration(), context);
                }
            });
        }

        public B subtask(final ManageableServerConfigurationTask.BaseBuilder<S, ManageableServerConfiguration, ?, ?> taskBuilder) {
            return subtask(new ResourceManagementSubtaskExecutor<S, R>() {
                @Override
                public void executeSubtasks(S source, R resourceManagement, TaskContext context) throws Exception {
                    final ServerMigrationTask subtask = taskBuilder.build(source, resourceManagement.getServerConfiguration());
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
            });
        }

        public B subtask(final BaseBuilder<S, R, ?, ?> taskBuilder) {
            return subtask(new ResourceManagementSubtaskExecutor<S, R>() {
                @Override
                public void executeSubtasks(S source, R resourceManagement, TaskContext context) throws Exception {
                    final ServerMigrationTask subtask = taskBuilder.build(source, resourceManagement);
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
            });
        }

        public abstract ServerMigrationTask build(final S source, final R... resourceManagements);
    }

    public static class Builder<S, R extends ResourceManagement> extends BaseBuilder<S, R, ResourceManagementSubtaskExecutor<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, R... resourceManagements) {
            return new ResourceManagementTask<>(this, source, resourceManagements);
        }
    }
}
