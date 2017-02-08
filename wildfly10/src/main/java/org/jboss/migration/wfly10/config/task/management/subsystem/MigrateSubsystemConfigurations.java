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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.extension.RemoveExtensionTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesCompositeTask;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

/**
 * @author emmartins
 */
public class MigrateSubsystemConfigurations<S> extends ManageableResourcesCompositeTask.Builder<S, ManageableResource> {

    public MigrateSubsystemConfigurations(String extension, String subsystem) {
        this(extension, new MigrateSubsystemConfigurationSubtaskBuilder<>(subsystem));
    }

    public MigrateSubsystemConfigurations(final String extensionModule, MigrateSubsystemConfigurationSubtaskBuilder<S> subtask) {
        name(new ServerMigrationTaskName.Builder("migrate-subsystem").addAttribute("name", subtask.getSubsystem()).build());
        skipPolicy(TaskSkipPolicy.skipByTaskEnvironment(EnvironmentProperties.getSubsystemTaskPropertiesPrefix(subtask.getSubsystem())));
        beforeRun(context -> context.getLogger().infof("Migrating subsystem %s configuration(s)...", subtask.getSubsystem()));
        subtasks(new ManageableResourceCompositeSubtasks.Builder<S, ManageableResource>()
                .subtask(SubsystemConfiguration.class, subtask.getSubsystem(), subtask)
                .subtask(new RemoveExtensionTaskBuilder<S>(extensionModule)));
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Subsystem %s configuration(s) migrated.", subtask.getSubsystem());
            } else {
                context.getLogger().infof("No subsystem %s configuration(s) migrated.", subtask.getSubsystem());
            }
        });
    }
}