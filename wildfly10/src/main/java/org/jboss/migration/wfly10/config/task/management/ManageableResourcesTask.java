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
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ResourceManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ManageableResourcesSubtaskExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class ManageableResourcesTask<S, R extends ManageableResources> extends ParentTask {

    protected final SubtasksExecutor<S, R>[] subtasks;
    protected final S source;
    protected final List<R> manageableResources;

    protected ManageableResourcesTask(BaseBuilder<S, R, ?, ?> builder, S source, List<R> manageableResources) {
        super(builder);
        this.subtasks = builder.subtasks.stream().toArray(SubtasksExecutor[]::new);
        this.source = source;
        this.manageableResources = manageableResources;
    }

    @Override
    protected void runSubtasks(TaskContext context) throws Exception {
        for (SubtasksExecutor<S, R> subtaskExecutor : subtasks) {
            subtaskExecutor.run(source, manageableResources, context);
        }
    }

    private interface SubtasksExecutor<S, R extends ManageableResources> {
        void run(S source, List<R> resourceManagements, TaskContext context) throws Exception;
    }

    protected abstract static class BaseBuilder<S, R extends ManageableResources, T extends ManageableResourcesSubtaskExecutor<S, R>, B extends BaseBuilder<S, R, T, B>> extends ParentTask.BaseBuilder<B> {

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
        public B subtask(ServerMigrationTask subtask) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                // only execute once
                subtask.run(context);
            });
        }

        @Override
        public B subtask(Subtasks subtasks) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                // only execute once
                subtasks.run(context);
            });
        }

        public B subtask(ResourceManagementSubtaskExecutor<S, R> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                // execute for all
                for (R r : resourceManagements) {
                    subtaskExecutor.executeSubtasks(source, r, context);
                }
            });
        }

        public <R1 extends ManageableResources> B subtask(ManageableResources.Query<R1> query, ResourceManagementSubtaskExecutor<S, R1> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                // collect all children
                List<R1> children = new ArrayList<>();
                for (R r : resourceManagements) {
                    children.addAll(r.findChildren(query));
                }
                // execute for all children
                for (R1 child : children) {
                    subtaskExecutor.executeSubtasks(source, child, context);
                }
            });
        }

        public <R1 extends ManageableResources> B subtask(Class<R1> childrenType, BaseBuilder<S, R1, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resources, context) -> {
                // collect all children
                List<R1> children = new ArrayList<>();
                for (R r : resources) {
                    children.addAll(r.getChildrenByType(childrenType));
                }
                // build for all children, and execute
                final ServerMigrationTask subtask = taskBuilder.build(source, children);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public B subtask(final ManageableServerConfigurationSubtaskExecutor<S, ManageableServerConfiguration> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                // execute per server config, not resources config
                Set<ManageableServerConfiguration> configs = new HashSet<>();
                for (R r : resourceManagements) {
                    configs.add(r.getServerConfiguration());
                }
                for (ManageableServerConfiguration config : configs) {
                    subtaskExecutor.run(source, config, context);
                }
            });
        }

        public B subtask(final ManageableServerConfigurationTask.BaseBuilder<S, ManageableServerConfiguration, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
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
            });
        }

        public B subtask(final BaseBuilder<S, R, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resourceManagements, context) -> {
                final ServerMigrationTask subtask = taskBuilder.build(source, resourceManagements);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public abstract ServerMigrationTask build(S source, List<R> resourceManagements);
    }

    public static class Builder<S, R extends ManageableResources> extends BaseBuilder<S, R, ManageableResourcesSubtaskExecutor<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, List<R> resourceManagements) {
            return new ManageableResourcesTask<>(this, source, resourceManagements);
        }
    }
}
