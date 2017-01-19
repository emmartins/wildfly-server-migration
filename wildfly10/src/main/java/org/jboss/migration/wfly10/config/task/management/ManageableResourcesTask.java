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
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ManageableResourceSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ManageableResourcesSubtaskExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class ManageableResourcesTask<S, R extends ManageableResource> extends ParentTask {

    protected final SubtasksExecutor<S, R>[] subtasks;
    protected final S source;
    protected final List<ManageableResources<R>> resourcesList;

    protected ManageableResourcesTask(BaseBuilder<S, R, ?, ?> builder, S source, List<ManageableResources<R>> resourcesList) {
        super(builder);
        this.subtasks = builder.subtasks.stream().toArray(SubtasksExecutor[]::new);
        this.source = source;
        this.resourcesList = resourcesList;
    }

    @Override
    protected void runSubtasks(TaskContext context) throws Exception {
        for (SubtasksExecutor<S, R> subtaskExecutor : subtasks) {
            subtaskExecutor.run(source, resourcesList, context);
        }
    }

    private interface SubtasksExecutor<S, R extends ManageableResource> {
        void run(S source, List<ManageableResources<R>> resourcesList, TaskContext context) throws Exception;
    }

    protected abstract static class BaseBuilder<S, R extends ManageableResource, T extends ManageableResourcesSubtaskExecutor<S, R>, B extends BaseBuilder<S, R, T, B>> extends ParentTask.BaseBuilder<B> {

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
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // only execute once
                subtask.run(context);
            });
        }

        @Override
        public B subtask(Subtasks subtasks) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // only execute once
                subtasks.run(context);
            });
        }

        public B subtask(ManageableResourcesSubtaskExecutor<S, R> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // execute for all
                for (ManageableResources<R> r : resourcesList) {
                    subtaskExecutor.run(source, r, context);
                }
            });
        }

        public <R1 extends ManageableResource> B subtask(ManageableResource.Type<R1> childrenType, ManageableResourceSubtaskExecutor<S, R1> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // collect all children
                List<R1> children = new ArrayList<>();
                for (ManageableResources<R> resources : resourcesList) {
                    for (R resource : resources.getResources()) {
                        for (ManageableResources<R1> childResources : resource.findResources(childrenType)) {
                            children.addAll(childResources.getResources());
                        }
                    }
                }
                // execute for all children
                for (R1 child : children) {
                    subtaskExecutor.executeSubtasks(source, child, context);
                }
            });
        }

        public <R1 extends ManageableResource> B subtask(ManageableResource.Type<R1> childrenType, ManageableResourceTask.BaseBuilder<S, R1, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // collect all children
                List<R1> children = new ArrayList<>();
                for (ManageableResources<R> resources : resourcesList) {
                    for (R resource : resources.getResources()) {
                        for (ManageableResources<R1> childResources : resource.findResources(childrenType)) {
                            children.addAll(childResources.getResources());
                        }
                    }
                }
                // build for all children, and execute
                final ServerMigrationTask subtask = taskBuilder.build(source, children);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public <R1 extends ManageableResource> B subtask(ManageableResource.Type<R1> childrenType, ManageableResourcesSubtaskExecutor<S, R1> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // collect all children
                List<ManageableResources<R1>> children = new ArrayList<>();
                for (ManageableResources<R> resources : resourcesList) {
                    for (R resource : resources.getResources()) {
                        children.addAll(resource.findResources(childrenType));
                    }
                }
                // execute for all children
                for (ManageableResources<R1> child : children) {
                    subtaskExecutor.run(source, child, context);
                }
            });
        }

        public <R1 extends ManageableResource> B subtask(ManageableResource.Type<R1> childrenType, ManageableResourcesTask.BaseBuilder<S, R1, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // collect all children
                List<ManageableResources<R1>> children = new ArrayList<>();
                for (ManageableResources<R> resources : resourcesList) {
                    for (R resource : resources.getResources()) {
                        children.addAll(resource.findResources(childrenType));
                    }
                }
                // build for all children, and execute
                final ServerMigrationTask subtask = taskBuilder.build(source, children);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public B subtask(final ManageableServerConfigurationSubtaskExecutor<S, ManageableServerConfiguration> subtaskExecutor) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // execute per server config, not resources config
                Set<ManageableServerConfiguration> configs = new HashSet<>();
                for (ManageableResources<R> resources : resourcesList) {
                    configs.add(resources.getServerConfiguration());
                }
                for (ManageableServerConfiguration config : configs) {
                    subtaskExecutor.run(source, config, context);
                }
            });
        }

        public B subtask(final ManageableServerConfigurationTask.BaseBuilder<S, ManageableServerConfiguration, ?, ?> taskBuilder) {
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                // build and execute per server config, not resources config
                Set<ManageableServerConfiguration> configs = new HashSet<>();
                for (ManageableResources<R> resources : resourcesList) {
                    configs.add(resources.getServerConfiguration());
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
            return subtask((SubtasksExecutor<S, R>) (source, resourcesList, context) -> {
                final ServerMigrationTask subtask = taskBuilder.build(source, resourcesList);
                if (subtask != null) {
                    context.execute(subtask);
                }
            });
        }

        public abstract ServerMigrationTask build(S source, List<ManageableResources<R>> resourcesList);
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<S, R, ManageableResourcesSubtaskExecutor<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, List<ManageableResources<R>> resourcesList) {
            return new ManageableResourcesTask<>(this, source, resourcesList);
        }
    }
}
