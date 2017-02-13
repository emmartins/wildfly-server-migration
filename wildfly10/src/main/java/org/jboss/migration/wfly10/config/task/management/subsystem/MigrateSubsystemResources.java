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
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.extension.RemoveExtensionTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesCompositeTask;

/**
 * @author emmartins
 */
public class MigrateSubsystemResources<S> extends ManageableResourcesCompositeTask.Builder<S, ManageableResource> {

    public MigrateSubsystemResources(String extension, String subsystem) {
        this(extension, new MigrateSubsystemResourceSubtaskBuilder<>(subsystem));
    }

    public MigrateSubsystemResources(final String extensionModule, MigrateSubsystemResourceSubtaskBuilder<S> subtask) {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("subsystem."+subtask.getSubsystem()+".migrate").build();
        name(taskName);
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().infof("Migrating subsystem %s configuration(s)...", subtask.getSubsystem()));
        subtasks(new ManageableResourcesCompositeSubtasks.Builder<S, ManageableResource>()
                .subtask(SubsystemResource.class, subtask.getSubsystem(), subtask.nameBuilder(parameters -> new ServerMigrationTaskName.Builder(taskName.getName()+".migrate-config").addAttribute("name", parameters.getResource().getResourceAbsoluteName()).build()))
                .subtask(new RemoveExtensionTaskBuilder<S>(extensionModule).name(new ServerMigrationTaskName.Builder(taskName.getName()+".remove-extension").addAttribute("module", extensionModule).build())));
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Subsystem %s configuration(s) migrated.", subtask.getSubsystem());
            } else {
                context.getLogger().infof("No subsystem %s configuration(s) migrated.", subtask.getSubsystem());
            }
        });
    }


}