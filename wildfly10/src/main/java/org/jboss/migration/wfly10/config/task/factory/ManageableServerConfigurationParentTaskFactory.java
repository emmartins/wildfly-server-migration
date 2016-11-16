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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;

import java.util.Collection;
import java.util.List;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationParentTaskFactory<S, T extends ManageableServerConfiguration> implements ManageableServerConfigurationTaskFactory<S, T> {

    private final List<ManageableServerConfigurationTaskFactory<S, T>> subtaskFactories;
    private final ServerMigrationTaskName taskName;
    private String skipTaskPropertyName;
    private ParentServerMigrationTask.EventListener eventListener;

    protected ManageableServerConfigurationParentTaskFactory(Builder<S, T> builder) {
        this.subtaskFactories = builder.taskFactories.getFactories();
        this.taskName = builder.taskName;
        this.skipTaskPropertyName = builder.skipTaskPropertyName;
        this.eventListener = builder.eventListener;
    }

    @Override
    public ServerMigrationTask getTask(final S source, final T configuration) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .succeedOnlyIfHasSuccessfulSubtasks();
        if (eventListener != null) {
            taskBuilder.eventListener(eventListener);
        }
        for (final ManageableServerConfigurationTaskFactory<S, T> subtaskFactory : subtaskFactories) {
            taskBuilder.subtask(subtaskFactory.getTask(source, configuration));
        }
        ServerMigrationTask task = taskBuilder.build();
        if (skipTaskPropertyName != null) {
            task = new SkippableByEnvServerMigrationTask(task, skipTaskPropertyName);
        }
        return task;
    }

    public static class Builder<S, T extends ManageableServerConfiguration> {

        private final ServerConfigurationMigration.ManageableServerConfigurationTaskFactories<S, T> taskFactories;
        private final ServerMigrationTaskName taskName;
        private String skipTaskPropertyName;
        private ParentServerMigrationTask.EventListener eventListener;

        public Builder(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
            this.taskFactories = new ServerConfigurationMigration.ManageableServerConfigurationTaskFactories<>();
        }

        public Builder<S, T> subtask(ManageableServerConfigurationTaskFactory<S, T> subtaskFactory) {
            taskFactories.add(subtaskFactory);
            return this;
        }

        public Builder<S, T> subtasks(Collection<ManageableServerConfigurationTaskFactory<S, T>> subtaskFactories) {
            taskFactories.addAll(subtaskFactories);
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

        public ManageableServerConfigurationParentTaskFactory<S, T> build() {
            return new ManageableServerConfigurationParentTaskFactory(this);
        }
    }
}
