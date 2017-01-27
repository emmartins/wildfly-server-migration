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

import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.task.management.extension.AddExtensionTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class AddSubsystemConfigurationTask<S> extends SubsystemConfigurationParentCompositeTask<S> {

    private AddSubsystemConfigurationTask(Builder<S> builder, S source, Collection<? extends ManageableResource> resources) {
        super(builder, source, resources);
    }

    public static class Builder<S> extends SubsystemConfigurationParentCompositeTask.BaseBuilder<S, Builder<S>> {

        public Builder(String extension, String subsystem) {
            this(extension, subsystem, new AddSubsystemConfigurationSubtask<>(subsystem));
        }

        public Builder(final String extension, final String subsystem, AddSubsystemConfigurationSubtask<S> subtask) {
            super(new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystem).build());
            listener(new AbstractServerMigrationTask.Listener() {
                @Override
                public void started(TaskContext context) {
                    context.getLogger().infof("Adding subsystem %s configuration(s)...", subsystem);
                }
                @Override
                public void done(TaskContext context) {
                    if (context.hasSucessfulSubtasks()) {
                        context.getLogger().infof("Subsystem %s configuration(s) added.", subsystem);
                    } else {
                        context.getLogger().infof("No subsystem %s configuration(s) added.", subsystem);
                    }
                }
            });
            subtask(new AddExtensionTask<>(extension));
            subtask(subtask);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new AddSubsystemConfigurationTask<>(this, source, resources);
        }
    }
}
