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

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.task.management.extension.RemoveExtensionTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class MigrateSubsystemConfigurationTask<S> extends SubsystemConfigurationTask<S> {

    private MigrateSubsystemConfigurationTask(Builder<S> builder, S source, Collection<? extends ManageableResource> resources) {
        super(builder, source, resources);
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {

        public Builder(String extension, String subsystem) {
            this(extension, subsystem, new MigrateSubsystemConfigurationSubtask<S>());
        }

        public Builder(final String extension, final String subsystem, MigrateSubsystemConfigurationSubtask<S> subtask) {
            super(subsystem, new ServerMigrationTaskName.Builder("migrate-subsystem").addAttribute("name", subsystem).build());
            listener(new Listener() {
                @Override
                public void started(TaskContext context) {
                    context.getLogger().infof("Migrating subsystem %s configuration(s)...", subsystem);
                }
                @Override
                public void done(TaskContext context) {
                    if (context.hasSucessfulSubtasks()) {
                        context.getLogger().infof("Subsystem %s configuration(s) migrated.", subsystem);
                    } else {
                        context.getLogger().infof("No subsystem %s configuration(s) migrated.", subsystem);
                    }
                }
            });
            subtask(subtask);
            subtask(new RemoveExtensionTask<S>(extension));
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new MigrateSubsystemConfigurationTask<>(this, source, resources);
        }
    }
}
