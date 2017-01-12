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
import org.jboss.migration.wfly10.config.task.management.ManageableServerConfigurationTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class ResourceManagementTask<S, R extends ResourceManagement> extends ParentTask {

    private static final SubtasksExecutor[] EMPTY = {};
    protected final SubtasksExecutor<S, R>[] subtasks;
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
        for (SubtasksExecutor<S, R> subtaskExecutor : subtasks) {
            subtaskExecutor.run(source, resourceManagements, context);
        }
    }

    private interface SubtasksExecutor<S, R extends ResourceManagement> {
        void run(S source, R[] resourceManagements, TaskContext context) throws Exception;
    }

    protected abstract static class BaseBuilder<S, R extends ResourceManagement, T extends ResourceManagementSubtaskExecutor<S, R>, B extends BaseBuilder<S, R, T, B>> extends ParentTask.BaseBuilder<B> {

        protected final List<SubtasksExecutor<S, R>> subtasks;

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
            this.subtasks = new ArrayList<>();
        }

        private B subtask(SubtasksExecutor<S, R> subtaskExecutor) {
            subtasks.add(subtaskExecutor);
            return (B) this;
        }

        @Override
        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    // only execute once
                    subtask.run(context);
                }
            });
        }

        @Override
        public B subtask(final Subtasks subtasks) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    // only execute once
                    subtasks.run(context);
                }
            });
        }

        public B subtask(final ResourceManagementSubtaskExecutor<S, R> subtaskExecutor) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    // execute for all
                    for (R r : resourceManagements) {
                        subtaskExecutor.executeSubtasks(source, r, context);
                    }
                }
            });
        }

        public B subtask(final ManageableServerConfigurationSubtaskExecutor<S, ManageableServerConfiguration> subtaskExecutor) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    // execute per server config, not resources config
                    Set<ManageableServerConfiguration> configs = new HashSet<>();
                    for (R r : resourceManagements) {
                        configs.add(r.getServerConfiguration());
                    }
                    for (ManageableServerConfiguration config : configs) {
                        subtaskExecutor.run(source, config, context);
                    }
                }
            });
        }

        public B subtask(final ManageableServerConfigurationTask.BaseBuilder<S, ManageableServerConfiguration, ?, ?> taskBuilder) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    // build and execute per server config, not resources config
                    Set<ManageableServerConfiguration> configs = new HashSet<>();
                    for (R r : resourceManagements) {
                        configs.add(r.getServerConfiguration());
                    }
                    for (ManageableServerConfiguration config : configs) {
                        final ServerMigrationTask subtask = taskBuilder.build(source, config);
                        if (subtask != null) {
                            context.execute(subtask);
                        }
                    }
                }
            });
        }

        public B subtask(final BaseBuilder<S, R, ?, ?> taskBuilder) {
            return subtask(new SubtasksExecutor<S, R>() {
                @Override
                public void run(S source, R[] resourceManagements, TaskContext context) throws Exception {
                    final ServerMigrationTask subtask = taskBuilder.build(source, resourceManagements);
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
