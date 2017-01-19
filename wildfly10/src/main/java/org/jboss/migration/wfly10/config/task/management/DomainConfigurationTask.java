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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.management.ProfileResources;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationTask;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationSubtasks;

import java.util.List;

/**
 * @author emmartins
 */
public class DomainConfigurationTask<S> extends ManageableServerConfigurationTask<S, HostControllerConfiguration> {

    protected DomainConfigurationTask(Builder<S> builder, S source, HostControllerConfiguration configuration) {
        super(builder, source, configuration);
    }

    public interface Subtasks<S> extends ManageableServerConfigurationTask.Subtasks<S, HostControllerConfiguration> {
    }

    public static class Builder<S> extends ManageableServerConfigurationTask.BaseBuilder<S, HostControllerConfiguration, Subtasks<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public Builder<S> subtask(SubsystemsConfigurationSubtasks<S> subtask) {
            return subtask((Subtasks<S>) (source, configuration, taskContext) -> {
                final ProfileResources profileResources = configuration.getProfileResources();
                for (String profileNames : profileResources.getResourceNames()) {
                    final ProfileResource profileResource = profileResources.getResource(profileNames);
                    subtask.executeSubtasks(source, profileResource.getSubsystemsManagement(), taskContext);
                }
            });
        }

        public Builder<S> subtask(SubsystemsConfigurationTask.Builder<S> builder) {
            return subtask((Subtasks<S>) (source, configuration, taskContext) -> {
                // replace with non generic resource retriever
                final List<SubsystemResources> resourceManagements = configuration.getResources(SubsystemResources.class);
                final ServerMigrationTask subtask = builder.build(source, resourceManagements);
                if (subtask != null) {
                    taskContext.execute(subtask);
                }
            });
        }

        @Override
        public ServerMigrationTask build(S source, HostControllerConfiguration configuration) {
            return new DomainConfigurationTask<>(this, source, configuration);
        }
    }
}
