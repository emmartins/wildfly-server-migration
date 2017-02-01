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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ParentServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.management.extension.AddExtensionTaskBuilder;

/**
 * A task which adds the jmx subsystem to host configs.
 * @author emmartins
 */
public class AddSubsystemTaskFactory<S> implements StandaloneServerConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S>, DomainConfigurationTaskFactory<S> {

    private final ServerMigrationTaskName taskName;
    private final AddExtensionTaskBuilder<S> addExtensionSubtask;
    private final AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;
    private String skipTaskPropertyName;
    private AbstractServerMigrationTask.Listener eventListener;

    protected AddSubsystemTaskFactory(final Builder<S> builder) {
        this.taskName = builder.taskName != null ? builder.taskName : new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name",builder. subsystemName).build();
        this.addExtensionSubtask = new AddExtensionTaskBuilder<>(builder.extensionName);
        this.addSubsystemConfigSubtask = builder.addSubsystemConfigSubtask != null ? builder.addSubsystemConfigSubtask : new AddSubsystemConfigSubtask<S>(builder.subsystemName);
        this.skipTaskPropertyName = builder.skipTaskPropertyName != null ? builder.skipTaskPropertyName : (taskName.getName()+".skip");
        this.eventListener = builder.eventListener != null ? builder.eventListener : new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding subsystem %s...", builder.subsystemName);
            }

            @Override
            public void done(TaskContext context) {
                context.getLogger().infof("Subsystem %s added.", builder.subsystemName);
            }
        };
    }

    @Override
    public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
        return getTask(source,
                SubtaskExecutorAdapters.of(source, configuration, addExtensionSubtask),
                SubtaskExecutorAdapters.of(source, configuration, addSubsystemConfigSubtask));
    }

    @Override
    public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
        return getTask(source,
                SubtaskExecutorAdapters.of(source, configuration, addExtensionSubtask),
                SubtaskExecutorAdapters.of(source, configuration, addSubsystemConfigSubtask));
    }

    @Override
    public ServerMigrationTask getTask(S source, StandaloneServerConfiguration configuration) throws Exception {
        return getTask(source,
                SubtaskExecutorAdapters.of(source, configuration, addExtensionSubtask),
                SubtaskExecutorAdapters.of(source, configuration, addSubsystemConfigSubtask));
    }

    public ServerMigrationTask getTask(S source, SubsystemResources subsystemResources) throws Exception {
        return getTask(source,
                SubtaskExecutorAdapters.of(source, subsystemResources.getServerConfiguration(), addExtensionSubtask),
                SubtaskExecutorAdapters.of(source, subsystemResources, addSubsystemConfigSubtask));
    }

    private ServerMigrationTask getTask(S source, ParentServerMigrationTask.SubtaskExecutor addExtensionSubtask, ParentServerMigrationTask.SubtaskExecutor addSubsystemSubtask) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .skipTaskPropertyName(skipTaskPropertyName)
                .subtask(addExtensionSubtask)
                .subtask(addSubsystemSubtask);
        if (eventListener != null) {
            taskBuilder.listener(eventListener);
        }
        return taskBuilder.build();
    }

    public static class Builder<S> {
        private ServerMigrationTaskName taskName;
        private final String subsystemName;
        private final String extensionName;
        private AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;
        private String skipTaskPropertyName;
        private AbstractServerMigrationTask.Listener eventListener;

        public Builder(String subsystemName, String extensionName) {
            this.subsystemName = subsystemName;
            this.extensionName = extensionName;
        }

        public Builder<S> eventListener(AbstractServerMigrationTask.Listener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder<S> skipTaskPropertyName(String skipTaskPropertyName) {
            this.skipTaskPropertyName = skipTaskPropertyName;
            return this;
        }

        public Builder<S> subtask(AddSubsystemConfigSubtask<S> subtask) {
            this.addSubsystemConfigSubtask = subtask;
            return this;
        }

        public Builder<S> taskName(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
            return this;
        }

        public AddSubsystemTaskFactory<S> build() {
            return new AddSubsystemTaskFactory(this);
        }
    }
}
