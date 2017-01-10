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

import com.sun.xml.internal.rngom.parse.host.Base;
import org.jboss.migration.core.ParentTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ExtensionsManagementSubtaskExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationTask<S, T extends ManageableServerConfiguration> extends ParentTask {

    protected ManageableServerConfigurationTask(BaseBuilder<S, T, ?> builder, List<ParentTask.Subtasks> subtasks) {
        super(builder, subtasks);
    }

    public interface Subtasks<S, T extends ManageableServerConfiguration> {
        void run(S source, T configuration, TaskContext taskContext) throws Exception;
    }

    protected static abstract class BaseBuilder<S, T extends ManageableServerConfiguration, B extends BaseBuilder<S, T, B>> extends ParentTask.BaseBuilder<Subtasks<S, T>, B> {

        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public B subtask(final BaseBuilder<S, T, ?> subtaskBuilder) {
            return subtask(new Subtasks<S, T>() {
                @Override
                public void run(S source, T configuration, TaskContext taskContext) throws Exception {
                    final ServerMigrationTask subtask = subtaskBuilder.build(source, configuration);
                    if (subtask != null) {
                        taskContext.execute(subtask);
                    }
                }
            });
        }

        public B subtask(final ExtensionsManagementSubtaskExecutor<S> subtask) {
            return subtask(new Subtasks<S, T>() {
                @Override
                public void run(S source, T configuration, TaskContext taskContext) throws Exception {
                    subtask.executeSubtasks(source, configuration.getExtensionsManagement(), taskContext);
                }
            });
        }

        public ServerMigrationTask build(final S source, final T configuration) {
            final List<ParentTask.Subtasks> subtasksList = new ArrayList<>();
            for (final Subtasks<S, T> subtasks : super.subtasks) {
                subtasksList.add(new ParentTask.Subtasks() {
                    @Override
                    public void run(TaskContext context) throws Exception {
                        subtasks.run(source, configuration, context);
                    }
                });
            }
            return build(subtasksList);
        }

        protected abstract ServerMigrationTask build(List<ParentTask.Subtasks> subtasks);
    }

    public static class Builder<S> extends BaseBuilder<S, ManageableServerConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        protected ServerMigrationTask build(List<ParentTask.Subtasks> subtasks) {
            return new ManageableServerConfigurationTask(this, subtasks);
        }
    }
}
