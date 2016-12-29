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
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ParentManageableServerConfigurationTaskFactory;

/**
 * @author emmartins
 */
public class AddSubsystemTaskFactory2 extends ParentServerMigrationTask {

    private final ServerMigrationTaskName taskName;
    private final AddExtensionSubtask<S> addExtensionSubtask;
    private final AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;

    protected AddSubsystemTaskFactory2(final Builder<S> builder) {
        this.taskName = builder.taskName != null ? builder.taskName : ;
        this.addExtensionSubtask = new AddExtensionSubtask<>(builder.extensionName);
        this.addSubsystemConfigSubtask = builder.addSubsystemConfigSubtask != null ? builder.addSubsystemConfigSubtask : new AddSubsystemConfigSubtask<S>(builder.subsystemName);
        this.skipTaskPropertyName = builder.skipTaskPropertyName != null ? builder.skipTaskPropertyName : (taskName.getName()+".skip");
        this.eventListener = builder.eventListener != null ? builder.eventListener :
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

    public static class Builder<S> {

        private final String subsystemName;
        private final String extensionName;
        private AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;

        public Builder(String subsystemName, String extensionName) {
            this.subsystemName = subsystemName;
            this.extensionName = extensionName;
            this.addSubsystemConfigSubtask = new AddSubsystemConfigSubtask<>(subsystemName);
        }

        public Builder<S> subtask(AddSubsystemConfigSubtask<S> subtask) {
            this.addSubsystemConfigSubtask = subtask;
            return this;
        }

    }

    public static Config
}
