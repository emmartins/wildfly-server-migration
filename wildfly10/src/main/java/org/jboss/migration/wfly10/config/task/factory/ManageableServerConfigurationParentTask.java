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

import org.jboss.migration.core.AbstractParentTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationParentTask<S, T extends ManageableServerConfiguration> extends AbstractParentTask<ManageableServerConfigurationParentTask.SubtaskExecutorContext<S, T>> {

    public ManageableServerConfigurationParentTask(BaseBuilder<SubtaskExecutorContext<S, T>, ?> builder, SubtaskExecutorContextFactory<SubtaskExecutorContext<S, T>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class Builder<S, T extends ManageableServerConfiguration> extends BaseBuilder<SubtaskExecutorContext<S,T>, Builder<S, T>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public ManageableServerConfigurationParentTask<S, T> build(final S source, final T configuration) {
            final SubtaskExecutorContextFactory<SubtaskExecutorContext<S,T>> subtaskExecutorContextFactory = new AbstractParentTask.SubtaskExecutorContextFactory<SubtaskExecutorContext<S, T>>() {
                @Override
                public SubtaskExecutorContext<S, T> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new SubtaskExecutorContext<>(context, source, configuration);
                }
            };
            return build(subtaskExecutorContextFactory);
        }

        @Override
        protected ManageableServerConfigurationParentTask<S, T> build(SubtaskExecutorContext<S, T> context) {
            return build(context.getSource(), context.getConfiguration());
        }

        @Override
        protected ManageableServerConfigurationParentTask<S, T> build(SubtaskExecutorContextFactory<SubtaskExecutorContext<S, T>> contextFactory) {
            return new ManageableServerConfigurationParentTask(this, contextFactory);
        }
    }

    public static class SubtaskExecutorContext<S, T extends ManageableServerConfiguration> extends TaskContextDelegate {
        private final S source;
        private final T configuration;

        private SubtaskExecutorContext(TaskContext taskContext, S source, T configuration) {
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
    }
}
