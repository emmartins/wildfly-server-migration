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
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.AddExtensionSubtask;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemConfigSubtask;

/**
 * @author emmartins
 */
public class AddSubsystemTaskBuilder<S> extends SubsystemConfigurationTaskBuilder<S> {

    public AddSubsystemTaskBuilder(String extension, String subsystem) {
        this(extension, subsystem, new AddSubsystemConfigSubtask<S>(subsystem));
    }

    public AddSubsystemTaskBuilder(final String extension, String subsystem, AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask) {
        super(extension, subsystem, new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystem).build());
        subtask(new SubtaskExecutor<S>(extension, subsystem, addSubsystemConfigSubtask));
    }

    private static class SubtaskExecutor<S> implements ParentTask.SubtaskExecutor<ResourceManagementParentTask.SubtaskExecutorContext<S, SubsystemsManagement>> {

        private final String extension;
        private final String subsystem;
        private final AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask;

        private SubtaskExecutor(String extension, String subsystem, AddSubsystemConfigSubtask<S> addSubsystemConfigSubtask) {
            this.extension = extension;
            this.subsystem = subsystem;
            this.addSubsystemConfigSubtask = addSubsystemConfigSubtask;
        }

        @Override
        public void run(ResourceManagementParentTask.SubtaskExecutorContext<S, SubsystemsManagement> context) throws Exception {
            final SubsystemsManagement subsystemsManagement = context.getResourceManagement();
            final ExtensionsManagement extensionsManagement = subsystemsManagement.getServerConfiguration().getExtensionsManagement();
            if (!extensionsManagement.getResourceNames().contains(extension)) {
                new AddExtensionSubtask<S>(extension).executeSubtasks(context.getSource(), extensionsManagement, context);
            }
        }
    }
}
