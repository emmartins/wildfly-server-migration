/*
 * Copyright 2016 Red Hat, Inc.
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
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationParentTask<S, T extends ManageableServerConfiguration> extends ParentTask<ManageableServerConfigurationParentTask.SubtaskContext<S, T>> {

    protected ManageableServerConfigurationParentTask(BaseBuilder<S, T, ?> builder, List<ContextFactory<SubtaskContext<S, T>>> contextFactories) {
        super(builder, contextFactories);
    }

    public interface Subtasks<S, T extends ManageableServerConfiguration> extends ParentTask.Subtasks<SubtaskContext<S, T>> {
    }

    protected static abstract class BaseBuilder<S, T extends ManageableServerConfiguration, B extends BaseBuilder<S, T, B>> extends ParentTask.BaseBuilder<SubtaskContext<S,T>, B> {

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public B subtask(final ExtensionsManagementParentTask.Subtasks<S> subtask) {
            return super.subtask(new Subtasks<S, T>() {
                @Override
                public void run(SubtaskContext<S, T> context) throws Exception {
                    subtask.run(context.toExtensionsManagementSubtasksContext());
                }
            });
        }
    }

    public static class Builder<S, T extends ManageableServerConfiguration> extends BaseBuilder<S, T, Builder<S, T>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public ManageableServerConfigurationParentTask build(final S source, final T configuration) {
            final ContextFactory<SubtaskContext<S, T>> contextFactory = new ContextFactory<SubtaskContext<S, T>>() {
                @Override
                public SubtaskContext<S, T> newInstance(TaskContext context) throws Exception {
                    return new SubtaskContext<>(context, source, configuration);
                }
            };
            return new ManageableServerConfigurationParentTask(this, Collections.singletonList(contextFactory));
        }
    }

    public static class SubtaskContext<S, T extends ManageableServerConfiguration> extends TaskContextDelegate {
        private final S source;
        private final T configuration;

        protected SubtaskContext(TaskContext taskContext, S source, T configuration) {
            super(taskContext);
            this.source = source;
            this.configuration = configuration;
        }

        public S getSource() {
            return source;
        }

        public T getConfiguration() {
            return configuration;
        }

        public ExtensionsManagementParentTask.SubtasksContext<S> toExtensionsManagementSubtasksContext() {
            return new ExtensionsManagementParentTask.SubtasksContext<>(taskContext, source, configuration.getExtensionsManagement());
        }
    }
}
