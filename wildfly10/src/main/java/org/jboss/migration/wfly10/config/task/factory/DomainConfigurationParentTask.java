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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextDelegate;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class DomainConfigurationParentTask<S> extends ParentTask<DomainConfigurationParentTask.SubtaskExecutorContext<S>> {

    public DomainConfigurationParentTask(BaseBuilder<SubtaskExecutorContext<S>, ?> builder, SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class Builder<S> extends BaseBuilder<SubtaskExecutorContext<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public DomainConfigurationParentTask<S> build(final S source, final HostControllerConfiguration configuration) {
            final SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> subtaskExecutorContextFactory = new SubtaskExecutorContextFactory<SubtaskExecutorContext<S, T>>() {
                @Override
                public SubtaskExecutorContext<S, T> getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return new SubtaskExecutorContext<>(context, source, configuration);
                }
            };
            return build(subtaskExecutorContextFactory);
        }

        @Override
        protected DomainConfigurationParentTask<S> build(SubtaskExecutorContext<S> context) {
            return build(context.getSource(), context.getConfiguration());
        }

        @Override
        protected DomainConfigurationParentTask<S> build(SubtaskExecutorContextFactory<SubtaskExecutorContext<S>> contextFactory) {
            return new DomainConfigurationParentTask(this, contextFactory);
        }
    }

    public static class SubtaskExecutorContext<S> extends TaskContextDelegate {
        private final S source;
        private final HostControllerConfiguration configuration;

        private SubtaskExecutorContext(TaskContext taskContext, S source, HostControllerConfiguration configuration) {
            super(taskContext);
            this.source = source;
            this.configuration = configuration;
        }

        public S getSource() {
            return source;
        }

        public HostControllerConfiguration getConfiguration() {
            return configuration;
        }
    }
}
