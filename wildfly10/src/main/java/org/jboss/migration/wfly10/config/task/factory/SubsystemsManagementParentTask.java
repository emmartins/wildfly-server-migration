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

import org.jboss.migration.core.AbstractParentTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.ResourceManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

/**
 * @author emmartins
 */
public class SubsystemsManagementParentTask<S> extends ResourceManagementParentTask<S, SubsystemsManagement> {

    public SubsystemsManagementParentTask(BaseBuilder<SubtaskExecutorContext<S>, ?> builder, SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class Builder<S> extends BaseBuilder<SubtaskExecutorContext<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public SubsystemsManagementParentTask<S> build(final S source, final SubsystemsManagement resourceManagement) {
            final SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> subtaskExecutorContextFactory = new SubtaskExecutorContextFactory<SubtaskExecutorContext<S>>() {
                @Override
                public SubtaskExecutorContext<S> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new SubtaskExecutorContext<>(context, source, resourceManagement);
                }
            };
            return build(subtaskExecutorContextFactory);
        }

        @Override
        protected SubsystemsManagementParentTask<S> build(SubtaskExecutorContext<S> context) {
            return build(context.getSource(), context.getResourceManagement());
        }

        @Override
        protected SubsystemsManagementParentTask<S> build(SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> contextFactory) {
            return new SubsystemsManagementParentTask(this, contextFactory);
        }
    }

    public static class SubtaskExecutorContext<S> extends ResourceManagementParentTask.SubtaskExecutorContext<S, SubsystemsManagement> {
        private SubtaskExecutorContext(TaskContext taskContext, S source, SubsystemsManagement resourceManagement) {
            super(taskContext, source, resourceManagement);
        }
    }
}
