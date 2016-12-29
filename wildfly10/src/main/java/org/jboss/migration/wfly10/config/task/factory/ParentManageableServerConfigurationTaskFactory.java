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

import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ParentManageableServerConfigurationTaskFactory<S, T extends ManageableServerConfiguration> implements ManageableServerConfigurationTaskFactory<S, T> {

    private final List<ManageableServerConfigurationSubtaskExecutor<S, T>> subtaskExecutors;
    private final ServerMigrationTaskName taskName;
    private String skipTaskPropertyName;
    private ParentServerMigrationTask.EventListener eventListener;

    protected ParentManageableServerConfigurationTaskFactory(Builder<S, T> builder) {
        this.subtaskExecutors = Collections.unmodifiableList(builder.taskFactories);
        this.taskName = builder.taskName;
        this.skipTaskPropertyName = builder.skipTaskPropertyName;
        this.eventListener = builder.eventListener;
    }

    @Override
    public ServerMigrationTask getTask(final S source, final T configuration) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .succeedOnlyIfHasSuccessfulSubtasks()
                .eventListener(eventListener)
                .skipTaskPropertyName(skipTaskPropertyName);
        for (final ManageableServerConfigurationSubtaskExecutor<S, T> subtaskExecutor : subtaskExecutors) {
            taskBuilder.subtask(new ParentServerMigrationTask.SubtaskExecutor() {
                @Override
                public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                    subtaskExecutor.executeSubtasks(source, configuration, context);
                }
            });
        }
        return taskBuilder.build();
    }

    public static class Builder<S, T extends ManageableServerConfiguration> {

        private final List<ManageableServerConfigurationSubtaskExecutor<S, T>> taskFactories;
        private final ServerMigrationTaskName taskName;
        private String skipTaskPropertyName;
        private ParentServerMigrationTask.EventListener eventListener;

        public Builder(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
            this.taskFactories = new ArrayList<>();
        }

        public Builder<S, T> subtask(final ManageableServerConfigurationTaskFactory<S, T> subtaskFactory) {
            return subtask(new ManageableServerConfigurationSubtaskExecutor<S, T>() {
                @Override
                public void executeSubtasks(S source, T configuration, ServerMigrationTaskContext context) throws Exception {
                   final ServerMigrationTask subtask = subtaskFactory.getTask(source, configuration);
                   if (subtask != null) {
                       context.execute(subtask);
                   }
                }
            });
        }

        public Builder<S, T> subtask(ManageableServerConfigurationSubtaskExecutor<S, T> subtaskExecutor) {
            taskFactories.add(subtaskExecutor);
            return this;
        }

        public Builder<S, T> eventListener(ParentServerMigrationTask.EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder<S, T> skipTaskPropertyName(String skipTaskPropertyName) {
            this.skipTaskPropertyName = skipTaskPropertyName;
            return this;
        }

        protected ServerMigrationTaskName getTaskName() {
            return taskName;
        }

        public ParentManageableServerConfigurationTaskFactory<S, T> build() {
            return new ParentManageableServerConfigurationTaskFactory(this);
        }
    }
}
