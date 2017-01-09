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
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class StandaloneServerConfigurationParentTask<S> extends ManageableServerConfigurationParentTask<S, StandaloneServerConfiguration> {

    protected StandaloneServerConfigurationParentTask(ManageableServerConfigurationParentTask.BaseBuilder<S, StandaloneServerConfiguration, ?> builder, List<ContextFactory<ManageableServerConfigurationParentTask.SubtaskContext<S, StandaloneServerConfiguration>>> contextFactories) {
        super(builder, contextFactories);
    }

    public static class Builder<S> extends ManageableServerConfigurationParentTask.BaseBuilder<S, StandaloneServerConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public StandaloneServerConfigurationParentTask build(final S source, final StandaloneServerConfiguration configuration) {
            final ContextFactory<SubtasksContext<S>> contextFactory = new ContextFactory<SubtasksContext<S>>() {
                @Override
                public SubtasksContext<S> newInstance(TaskContext context) throws Exception {
                    return new SubtasksContext<>(context, source, configuration);
                }
            };
            return new StandaloneServerConfigurationParentTask(this, Collections.singletonList(contextFactory));
        }

        public Builder<S> subtask(final SubsystemsManagementTask.Subtasks<S> subtask) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(SubtasksContext<S> context) throws Exception {
                    subtask.run(new SubsystemsManagementTask.SubtasksContext<>(context));
                }
            });
        }
    }

    public interface Subtasks<S> extends ParentTask.Subtasks<SubtasksContext<S>> {
    }

    public static class SubtasksContext<S> extends ManageableServerConfigurationParentTask.SubtaskContext<S, StandaloneServerConfiguration> {
        private SubtasksContext(TaskContext taskContext, S source, StandaloneServerConfiguration configuration) {
            super(taskContext, source, configuration);
        }
    }
}
