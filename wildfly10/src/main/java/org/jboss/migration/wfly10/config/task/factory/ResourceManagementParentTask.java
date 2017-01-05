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

/**
 * @author emmartins
 */
public class ResourceManagementParentTask<S, R extends ResourceManagement> extends ParentTask<ResourceManagementParentTask.SubtaskExecutorContext<S, R>> {

    public ResourceManagementParentTask(Builder<S, R> builder, SubtaskExecutorContextFactory<ResourceManagementParentTask.SubtaskExecutorContext<S, R>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class Builder<S, R extends ResourceManagement> extends BaseBuilder<ResourceManagementParentTask.SubtaskExecutorContext<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public ResourceManagementParentTask<S, R> build(final S source, final R resourceManagement) {
            final SubtaskExecutorContextFactory<ResourceManagementParentTask.SubtaskExecutorContext<S, R>> subtaskExecutorContextFactory = new SubtaskExecutorContextFactory<ResourceManagementParentTask.SubtaskExecutorContext<S, R>>() {
                @Override
                public ResourceManagementParentTask.SubtaskExecutorContext<S, R> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new SubtaskExecutorContext<S, R>(context, source, resourceManagement);
                }
            };
            return build(subtaskExecutorContextFactory);
        }

        @Override
        protected ResourceManagementParentTask<S, R> build(ResourceManagementParentTask.SubtaskExecutorContext<S, R> context) {
            return build(context.getSource(), context.getResourceManagement());
        }

        @Override
        protected ResourceManagementParentTask<S, R> build(SubtaskExecutorContextFactory<ResourceManagementParentTask.SubtaskExecutorContext<S, R>> contextFactory) {
            return new ResourceManagementParentTask(this, contextFactory);
        }
    }

    public static class SubtaskExecutorContext<S, R extends ResourceManagement> extends TaskContextDelegate {
        private final S source;
        private final R resourceManagement;

        protected SubtaskExecutorContext(TaskContext taskContext, S source, R resourceManagement) {
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
}
