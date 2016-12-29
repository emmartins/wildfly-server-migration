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

import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ParentManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

/**
 * A task which adds the jmx subsystem to host configs.
 * @author emmartins
 */
public class AddSubsystemTaskFactory<S, T extends ManageableServerConfiguration> extends ParentManageableServerConfigurationTaskFactory<S, T> {

    private final ServerMigrationTaskName taskName;
    private final AddExtensionSubtask<S> addExtensionSubtask;
    private final AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;

    protected AddSubsystemTaskFactory(final Builder<S> builder) {
        this.taskName = builder.taskName != null ? builder.taskName : ;
        this.addExtensionSubtask = new AddExtensionSubtask<>(builder.extensionName);
        this.addSubsystemConfigSubtask = builder.addSubsystemConfigSubtask != null ? builder.addSubsystemConfigSubtask : new AddSubsystemConfigSubtask<S>(builder.subsystemName);
        this.skipTaskPropertyName = builder.skipTaskPropertyName != null ? builder.skipTaskPropertyName : (taskName.getName()+".skip");
        this.eventListener = builder.eventListener != null ? builder.eventListener : new ParentServerMigrationTask.EventListener() {
            @Override
            public void started(ServerMigrationTaskContext context) {
                context.getLogger().infof("Adding subsystem %s...", builder.subsystemName);
            }
            @Override
            public void done(ServerMigrationTaskContext context) {
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

    public ServerMigrationTask getTask(S source, SubsystemsManagement subsystemsManagement) throws Exception {
        return getTask(source,
                SubtaskExecutorAdapters.of(source, subsystemsManagement.getServerConfiguration(), addExtensionSubtask),
                SubtaskExecutorAdapters.of(source, subsystemsManagement, addSubsystemConfigSubtask));
    }

    private ServerMigrationTask getTask(S source, ParentServerMigrationTask.SubtaskExecutor addExtensionSubtask, ParentServerMigrationTask.SubtaskExecutor addSubsystemSubtask) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .subtask(addExtensionSubtask)
                .subtask(addSubsystemSubtask);
        if (eventListener != null) {
            taskBuilder.eventListener(eventListener);
        }
        return new SkippableByEnvServerMigrationTask(taskBuilder.build(), skipTaskPropertyName);
    }

    public static class Builder<S, T extends ManageableServerConfiguration> extends ParentManageableServerConfigurationTaskFactory.Builder<S, T> {
        private final String subsystemName;
        private final String extensionName;
        private AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;
        private String skipTaskPropertyName;
        private ParentServerMigrationTask.EventListener eventListener;

        public Builder(String subsystemName, String extensionName) {
            this(subsystemName, extensionName, )
        }

        public Builder(String subsystemName, String extensionName, AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask) {
            super(new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystemName).build());
            this.subsystemName = subsystemName;
            this.extensionName = extensionName;
            subtask(new ManageableServerConfigurationTaskFactory<S, T>() {
                @Override
                public ServerMigrationTask getTask(S source, T configuration) throws Exception {
                    return null;
                }
            });
        }

        public Builder<S> eventListener(ParentServerMigrationTask.EventListener eventListener) {
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
