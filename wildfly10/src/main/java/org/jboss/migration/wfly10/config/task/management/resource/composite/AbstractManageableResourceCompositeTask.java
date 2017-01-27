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

package org.jboss.migration.wfly10.config.task.management.resource.composite;

import org.jboss.migration.core.task.component.AbstractCompositeTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.management.resource.component.AbstractManageableResourceComponentTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public abstract class AbstractManageableResourceCompositeTask<S, R extends ManageableResource> extends AbstractCompositeTask {

    private final S source;
    private final Collection<? extends R> resources;

    protected AbstractManageableResourceCompositeTask(Builder<S, R, ?, ?> builder) {
        super(builder);
        this.source = builder.source;
        this.resources = builder.resources;
    }

    public S getSource() {
        return source;
    }

    public Collection<? extends R> getResources() {
        return resources;
    }

    protected interface SubtaskExecutor<S, R extends ManageableResource, T extends AbstractManageableResourceCompositeTask<S, R>> {
        void run(Collection<? extends R> resources, T parentTask, TaskContext parentTaskContext) throws Exception;
    }

    protected interface SubtaskFactory<S, R extends ManageableResource, T extends AbstractManageableResourceCompositeTask<S, R>> {
        ServerMigrationTask getTask(R resource, T parentTask, TaskContext parentTaskContext) throws Exception;
    }

    protected interface Runnable<S, R extends ManageableResource, T extends AbstractManageableResourceCompositeTask<S, R>> extends AbstractCompositeTask.Runnable<T> {
        void subtask(SubtaskExecutor<S, ? super R, T> subtask);
        void subtask(SubtaskFactory<S, ? super R, T> subtaskFactory);
    }

    protected static class BasicRunnable<S, R extends ManageableResource, T extends AbstractManageableResourceCompositeTask<S, R>> extends AbstractCompositeTask.BasicRunnable<T> implements Runnable<S, R, T> {

        protected Collection<? extends R> getResources(T task) {
            return task.getResources();
        }

        public void subtask(SubtaskExecutor<S, ? super R, T> subtask) {
            final AbstractCompositeTask.SubtaskExecutor<T> subtaskExecutor = (parentTask, parentTaskContext) -> subtask.run(getResources(parentTask), parentTask, parentTaskContext);
            super.subtask(subtaskExecutor);
        }

        public void subtask(SubtaskFactory<S, ? super R, T> subtaskFactory) {
            final AbstractCompositeTask.SubtaskExecutor<T> subtaskExecutor = (parentTask, parentTaskContext) -> {
                for (R resource : getResources(parentTask)) {
                    final ServerMigrationTask subtask = subtaskFactory.getTask(resource, parentTask, parentTaskContext);
                    if (subtask != null) {
                        parentTaskContext.execute(subtask);
                    }
                }
            };
            super.subtask(subtaskExecutor);
        }

        @Override
        public ServerMigrationTaskResult run(T task, TaskContext context) throws Exception {
            if (getResources(task).isEmpty()) {
                context.getLogger().infof("Skipping task %s, no resources selected.", task.getName());
                return ServerMigrationTaskResult.SKIPPED;
            }
            return super.run(task, context);
        }
    }

    protected static abstract class Builder<S, R extends ManageableResource, T extends AbstractManageableResourceCompositeTask<S, R>, B extends Builder<S, R, T, B>> extends AbstractCompositeTask.Builder<T, B> implements ManageableServerConfigurationTaskFactory<S, ManageableServerConfiguration> {

        private S source;
        private Collection<? extends R> resources;
        private final ManageableResourceSelector<R> selector;
        private final Runnable<S, R, T> runnable;

        protected Builder(ServerMigrationTaskName name, Class<R> resourceType) {
            this(name, ManageableResourceSelectors.selectResources(resourceType));
        }

        protected Builder(NameFactory<? super T> nameFactory, Class<R> resourceType) {
            this(nameFactory, ManageableResourceSelectors.selectResources(resourceType));
        }

        protected Builder(ServerMigrationTaskName name, ManageableResourceSelector<R> selector) {
            this(task -> name, selector);
        }

        protected Builder(NameFactory<? super T> nameFactory, ManageableResourceSelector<R> selector) {
            this(nameFactory, new BasicRunnable<>(), selector);
        }

        protected Builder(NameFactory<? super T> nameFactory, Runnable<S, R, T> runnable, ManageableResourceSelector<R> selector) {
            super(nameFactory, runnable);
            this.selector = selector;
            this.runnable = runnable;
        }

        protected Builder(Builder<S, R, T, ?> other) {
            super(other);
            this.source = other.source;
            this.resources = other.resources;
            this.selector = other.selector;
            this.runnable = other.runnable;
        }

        public B source(S source) {
            this.source = source;
            return (B) this;
        }

        public B resources(Collection<? extends ManageableResource> resources) throws IOException {
            this.resources = Collections.unmodifiableSet(selector.fromResources(resources));
            return (B) this;
        }

        public B resources(ManageableResource resource) throws IOException {
            return resources(Collections.singleton(resource));
        }

        protected B subtask(SubtaskExecutor<S, ? super R, T> subtask) {
            runnable.subtask(subtask);
            return (B) this;
        }

        protected B subtask(SubtaskFactory<S, ? super R, T> subtask) {
            runnable.subtask(subtask);
            return (B) this;
        }

        protected B subtask(Builder<S, ? super R, T, ?> subtask) {
            final Builder<S, ? super R, T, ?> clone = subtask.clone();
            final SubtaskExecutor<S, R, T> subtaskExecutor = (resources, parentTask, parentTaskContext) -> parentTaskContext.execute(clone.source(parentTask.getSource()).resources(resources).build());
            return subtask(subtaskExecutor);
        }

        protected <R1 extends ManageableResource> B subtask(Class<R1> resourceType, SubtaskExecutor<S, R1, ? super T> subtask) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), subtask);
        }

        protected  <R1 extends ManageableResource> B subtask(ManageableResourceSelector<R1> resourceSelector, SubtaskExecutor<S, R1, ? super T> subtask) {
            // apply selector and run for all resources selected
            final SubtaskExecutor<S, R, T> subtaskExecutor = (resources, task, context) -> subtask.run(resourceSelector.fromResources(resources), task, context);
            return subtask(subtaskExecutor);
        }

        protected  <R1 extends ManageableResource> B subtask(Class<R1> resourceType, SubtaskFactory<S, R1, ? super T> subtask) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), subtask);
        }

        protected  <R1 extends ManageableResource> B subtask(ManageableResourceSelector<R1> resourceSelector, SubtaskFactory<S, R1, ? super T> subtaskFactory) {
            // apply selector and run for all resources selected
            final SubtaskExecutor<S, R, T> subtasksExecutor = (resources, task, context) -> {
                for (R1 resource : resourceSelector.fromResources(resources)) {
                    final ServerMigrationTask subtask = subtaskFactory.getTask(resource, task, context);
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
            };
            return subtask(subtasksExecutor);
        }

        protected B subtask(AbstractManageableResourceComponentTask.Builder<S, ? super R, ?, ?> subtask) {
            final AbstractManageableResourceComponentTask.Builder<S, ? super R, ?, ?> clone = subtask.clone();
            final SubtaskExecutor<S, R, T> subtaskExecutor = (resources, task, context) -> {
                for (R resource : resources) {
                    context.execute(clone.source(task.getSource()).resource(resource).build());
                }
            };
            return subtask(subtaskExecutor);
        }

        protected <R1 extends ManageableResource> B subtask(Class<R1> resourceType, AbstractManageableResourceComponentTask.Builder<S, ? super R1, ?, ?> subtask) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), subtask);
        }

        protected <R1 extends ManageableResource> B subtask(ManageableResourceSelector<R1> resourceSelector, AbstractManageableResourceComponentTask.Builder<S, ? super R1, ?, ?> subtask) {
            final AbstractManageableResourceComponentTask.Builder<S, ? super R1, ?, ?> clone = subtask.clone();
            // apply selector and build for all resources selected
            final SubtaskExecutor<S, R, T> subtasksExecutor = (resources, task, context) -> {
                for (R1 resource : resourceSelector.fromResources(resources)) {
                    context.execute(clone.source(task.getSource()).resource(resource).build());
                }
            };
            return subtask(subtasksExecutor);
        }

        @Override
        public ServerMigrationTask getTask(S source, ManageableServerConfiguration configuration) throws Exception {
            return clone().source(source).resources(configuration).build();
        }
    }
}
