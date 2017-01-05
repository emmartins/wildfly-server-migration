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

import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

/**
 * @author emmartins
 */
public class SubsystemsManagementParentTask<S> extends ResourceManagementParentTask<S, SubsystemsManagement> {

    protected SubsystemsManagementParentTask(Builder<S> builder, SubtaskExecutorContextFactory<ResourceManagementParentTask.SubtaskExecutorContext<S, SubsystemsManagement>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    /*
    public static class SubtaskExecutorContext<S> extends ResourceManagementParentTask.SubtaskExecutorContext<S, SubsystemsManagement> {
        private SubtaskExecutorContext(TaskContext taskContext, S source, SubsystemsManagement resourceManagement) {
            super(taskContext, source, resourceManagement);
        }
    }
    */

    public static class Builder<S> extends ResourceManagementParentTask.Builder<S, SubsystemsManagement> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected SubsystemsManagementParentTask<S> build(SubtaskExecutorContextFactory<SubtaskExecutorContext<S, SubsystemsManagement>> contextFactory) {
            return new SubsystemsManagementParentTask(this, contextFactory);
        }

        /*
        public SubsystemsManagementParentTask<S> build(final S source, final SubsystemsManagement resourceManagement) {
            final SubtaskExecutorContextFactory<SubtaskExecutorContext<S, SubsystemsManagement>> subtaskExecutorContextFactory = new SubtaskExecutorContextFactory<SubtaskExecutorContext<S, SubsystemsManagement>>() {
                @Override
                public SubtaskExecutorContext<S> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new SubtaskExecutorContext<>(context, source, resourceManagement);
                }
            };
            return build(subtaskExecutorContextFactory);
        }

        @Override
        protected SubsystemsManagementParentTask<S> build(SubtaskExecutorContext<S, SubsystemsManagement> context) {
            return build(context.getSource(), context.getResourceManagement());
        }
        */
    }
}
