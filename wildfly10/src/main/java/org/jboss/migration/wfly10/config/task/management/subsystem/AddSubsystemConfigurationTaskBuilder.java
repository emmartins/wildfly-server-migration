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
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.extension.AddExtensionTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesCompositeTask;

/**
 * @author emmartins
 */
public class AddSubsystemConfigurationTaskBuilder<S> extends ResourcesCompositeTask.Builder<S, SubsystemConfiguration.Parent> {

    public AddSubsystemConfigurationTaskBuilder(String extension, String subsystem) {
        this(extension, new AddSubsystemConfigurationSubtaskBuilder<>(subsystem));
    }

    public AddSubsystemConfigurationTaskBuilder(final String extension, AddSubsystemConfigurationSubtaskBuilder<S> subtask) {
        name(new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subtask.getSubsystem()).build());
        beforeRun(context -> context.getLogger().infof("Adding subsystem %s configuration(s)...", subtask.getSubsystem()));
        subtasks(new ResourcesCompositeSubtasks.Builder<S, SubsystemConfiguration.Parent>()
                .subtask(new AddExtensionTaskBuilder<S>(extension))
                .subtask(subtask));
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Subsystem %s configuration(s) added.", subtask.getSubsystem());
            } else {
                context.getLogger().infof("No subsystem %s configuration(s) added.", subtask.getSubsystem());
            }
        });
    }
}
