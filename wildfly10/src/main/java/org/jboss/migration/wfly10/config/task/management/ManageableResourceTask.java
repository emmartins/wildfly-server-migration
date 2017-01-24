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

import org.jboss.migration.core.CompositeTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableResourceTask<S, R extends ManageableResource> extends CompositeTask {

    protected final SubtaskExecutor[] subtaskExecutors;
    protected final S source;
    protected final Collection<? extends R> manageableResources;

    protected ManageableResourceTask(BaseBuilder<S, R, ?> builder, S source, Collection<? extends R> manageableResources) {
        super(builder);
        this.subtaskExecutors = builder.subtasks.stream().toArray(SubtaskExecutor[]::new);
        this.source = source;
        this.manageableResources = manageableResources;
    }

    @Override
    protected void runSubtasks(TaskContext context) throws Exception {
        for (SubtaskExecutor subtaskExecutor : subtaskExecutors) {
            subtaskExecutor.run(source, manageableResources, context);
        }
    }

    public interface SubtaskExecutor<S, R extends ManageableResource> {
        void run(S source, Collection<? extends R> resources, TaskContext context) throws Exception;
    }

    protected abstract static class BaseBuilder<S, R extends ManageableResource, B extends BaseBuilder<S, R, B>> extends CompositeTask.BaseBuilder<B> {

        protected final List<SubtaskExecutor<S, ? super R>> subtasks;

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
            this.subtasks = new ArrayList<>();
        }

        public B subtask(SubtaskExecutor<S, ? super R> subtaskExecutor) {
            subtasks.add(subtaskExecutor);
            return (B) this;
        }

        @Override
        public B subtask(ServerMigrationTask subtask) {
            // only execute once
            final SubtaskExecutor<S, R> subtaskExecutor = (source, resources, context) -> subtask.run(context);
            return subtask(subtaskExecutor);
        }

        @Override
        public B subtask(CompositeTask.SubtaskExecutor subtask) {
            // only execute once
            final SubtaskExecutor<S, R> subtaskExecutor = (source, resources, context) -> subtask.run(context);
            return subtask(subtaskExecutor);
        }

        public <R1 extends ManageableResource> B subtask(ManageableResourceSelector<R1> resourceSelector, SubtaskExecutor<S, R1> subtaskExecutor) {
            // apply selector and run for all resources selected
            final SubtaskExecutor<S, R> subtasksExecutor = (source, resources, context) -> subtaskExecutor.run(source, resourceSelector.collect(resources), context);
            return subtask(subtasksExecutor);
        }

        public <R1 extends ManageableResource> B subtask(ManageableResourceSelector<R1> resourceSelector, BaseBuilder<S, R1, ?> taskBuilder) {
            final SubtaskExecutor<S, R> subtasksExecutor = taskBuilder.toExecutor(resourceSelector);
            return subtask(subtasksExecutor);
        }

        public B subtask(BaseBuilder<S, ? super R, ?> taskBuilder) {
            final SubtaskExecutor<S, R> subtaskExecutor = taskBuilder.toExecutor();
            return subtask(subtaskExecutor);
        }

        public <R1 extends ManageableResource> SubtaskExecutor<S, R1> toExecutor(ManageableResourceSelector<R> resourceSelector) {
            return (source, resources, context) -> {
                final ServerMigrationTask task = build(source, resourceSelector.collect(resources));
                if (task != null) {
                    context.execute(task);
                }
            };
        }

        public <R1 extends R> SubtaskExecutor<S, R1> toExecutor() {
            return (source, resources, context) -> {
                final ServerMigrationTask task = build(source, resources);
                if (task != null) {
                    context.execute(task);
                }
            };
        }

        public ServerMigrationTask build(S source, R resource) {
            return build(source, Collections.singleton(resource));
        }

        public abstract ServerMigrationTask build(S source, Collection<? extends R> resources);
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<S, R, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends R> resources) {
            return new ManageableResourceTask<S, R>(this, source, resources);
        }
    }



}
