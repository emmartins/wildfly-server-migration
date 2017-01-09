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
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;

import java.util.List;

/**
 * @author emmartins
 */
public class ExtensionsManagementParentTask<S> extends ResourceManagementParentTask<S, ExtensionsManagement, ExtensionsManagementParentTask.SubtasksContext<S>> {

    protected ExtensionsManagementParentTask(Builder<S> builder, List<ContextFactory<SubtasksContext<S>>> contextFactories) {
        super(builder, contextFactories);
    }

    public static class Builder<S> extends ResourceManagementParentTask.BaseBuilder<S, ExtensionsManagement, SubtasksContext<S>, ExtensionsManagementParentTask<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ContextFactory<SubtasksContext<S>> getContextFactory(final S source, final ExtensionsManagement resourceManagement) {
            return new ContextFactory<SubtasksContext<S>>() {
                @Override
                public SubtasksContext<S> newInstance(TaskContext context) throws Exception {
                    return new ExtensionsManagementParentTask.SubtasksContext<>(context, source, resourceManagement);
                }
            };
        }

        protected ExtensionsManagementParentTask<S> build(List<ContextFactory<SubtasksContext<S>>> contextFactories) {
            return new ExtensionsManagementParentTask<>(this, contextFactories);
        }
    }

    public interface Subtasks<S> extends ResourceManagementParentTask.Subtasks<S, ExtensionsManagement, SubtasksContext<S>> {
    }

    public static class SubtasksContext<S> extends ResourceManagementParentTask.SubtasksContext<S, ExtensionsManagement> {
        public SubtasksContext(TaskContext taskContext, S source, ExtensionsManagement resourceManagement) {
            super(taskContext, source, resourceManagement);
        }
    }
}
