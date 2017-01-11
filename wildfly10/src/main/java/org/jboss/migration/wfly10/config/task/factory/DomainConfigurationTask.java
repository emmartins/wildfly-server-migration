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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class DomainConfigurationTask<S> extends ManageableServerConfigurationTask<S, HostControllerConfiguration> {

    protected DomainConfigurationTask(Builder<S> builder, S source, HostControllerConfiguration configuration) {
        super(builder, source, configuration);
    }

    public interface Subtasks<S> extends ManageableServerConfigurationSubtaskExecutor<S, HostControllerConfiguration> {
    }

    public static class Builder<S> extends ManageableServerConfigurationTask.BaseBuilder<S, HostControllerConfiguration, Subtasks<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        public Builder<S> subtask(final SubsystemsManagementSubtaskExecutor<S> subtask) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostControllerConfiguration configuration, TaskContext taskContext) throws Exception {
                    final ProfilesManagement profilesManagement = configuration.getProfilesManagement();
                    for (String profileNames : profilesManagement.getResourceNames()) {
                        final ProfileManagement profileManagement = profilesManagement.getProfileManagement(profileNames);
                        subtask.executeSubtasks(source, profileManagement.getSubsystemsManagement(), taskContext);
                    }
                }
            });
        }

        public Builder<S> subtask(final SubsystemsManagementTask.Builder<S> builder) {
            return subtask(new Subtasks<S>() {
                @Override
                public void run(S source, HostControllerConfiguration configuration, TaskContext taskContext) throws Exception {
                    final List<SubsystemsManagement> resourceManagements = new ArrayList<>();
                    final ProfilesManagement profilesManagement = configuration.getProfilesManagement();
                    for (String profileNames : profilesManagement.getResourceNames()) {
                        resourceManagements.add(profilesManagement.getProfileManagement(profileNames).getSubsystemsManagement());
                    }
                    final ServerMigrationTask subtask = builder.build(source, resourceManagements.toArray(new SubsystemsManagement[0]));
                    if (subtask != null) {
                        taskContext.execute(subtask);
                    }
                }
            });
        }

        @Override
        public ServerMigrationTask build(S source, HostControllerConfiguration configuration) {
            return new DomainConfigurationTask<>(this, source, configuration);
        }
    }
}
