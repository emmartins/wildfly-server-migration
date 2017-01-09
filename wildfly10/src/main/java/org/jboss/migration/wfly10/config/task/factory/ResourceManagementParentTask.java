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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.ResourceManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ResourceManagementParentTask<S, R extends ResourceManagement, C extends ResourceManagementParentTask.SubtasksContext<S, R>> extends ParentTask<C> {

    protected ResourceManagementParentTask(BaseBuilder<S, R, C, ?, ?> builder, List<ContextFactory<C>> contextFactories) {
        super(builder, contextFactories);
    }

    protected static abstract class BaseBuilder<S, R extends ResourceManagement, C extends ResourceManagementParentTask.SubtasksContext<S, R>, T extends ResourceManagementParentTask<S, R, C>, B extends BaseBuilder<S, R, C, T, B>> extends ParentTask.BaseBuilder<C, B> {

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        protected abstract ContextFactory<C> getContextFactory(final S source, final R resourceManagement);
        /*{
            return new ContextFactory<C>() {
                @Override
                public C newInstance(TaskContext context) throws Exception {
                    return new SubtasksContext<>(context, source, resourceManagement);
                }
            };
        }*/

        protected List<ContextFactory<C>> getContextFactories(final S source, final R... resourceManagements) {
            if (resourceManagements == null || resourceManagements.length == 0) {
                return null;
            } else if (resourceManagements.length == 1) {
                return Collections.singletonList(getContextFactory(source, resourceManagements[0]));
            } else {
                final List<ContextFactory<C>> contextFactories = new ArrayList<>();
                for (final R resourceManagement : resourceManagements) {
                    contextFactories.add(getContextFactory(source, resourceManagement));
                }
                return contextFactories;
            }
        }

        public T build(S source, R... resourceManagements) {
            return build(getContextFactories(source, resourceManagements));
        }

        protected T build(C context) {
            return build(context.getSource(), context.getResourceManagement());
        }

        protected abstract T build(List<ContextFactory<C>> contextFactories);
    }

    public static class Builder<S, R extends ResourceManagement> extends BaseBuilder<S, R, SubtasksContext<S, R>, ResourceManagementParentTask<S, R, SubtasksContext<S, R>>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ContextFactory<SubtasksContext<S, R>> getContextFactory(final S source, final R resourceManagement) {
            return new ContextFactory<SubtasksContext<S, R>>() {
                @Override
                public SubtasksContext<S, R> newInstance(TaskContext context) throws Exception {
                    return new SubtasksContext<>(context, source, resourceManagement);
                }
            };
        }

        @Override
        protected ResourceManagementParentTask<S, R, SubtasksContext<S, R>> build(List<ContextFactory<SubtasksContext<S, R>>> contextFactories) {
            return new ResourceManagementParentTask(this, contextFactories);
        }
    }

    public static class SubtasksContext<S, R extends ResourceManagement> extends TaskContextDelegate {
        private final S source;
        private final R resourceManagement;

        protected SubtasksContext(TaskContext taskContext, S source, R resourceManagement) {
            super(taskContext);
            this.source = source;
            this.resourceManagement = resourceManagement;
        }

        public S getSource() {
            return source;
        }

        public R getResourceManagement() {
            return resourceManagement;
        }
    }

    public interface Subtasks<S, R extends ResourceManagement, C extends ResourceManagementParentTask.SubtasksContext<S, R>> extends ParentTask.Subtasks<C> {

    }

}
