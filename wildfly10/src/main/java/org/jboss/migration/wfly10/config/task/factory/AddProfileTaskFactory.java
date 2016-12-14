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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemTaskFactory;

/**
 * @author emmartins
 */
public class AddProfileTaskFactory<S> extends ParentManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> {

    public AddProfileTaskFactory(Builder<S> builder) {
        super(builder);
    }

    public static class CreateProfileTask<S> implements ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> {
        private final String profileName;
        private final ServerMigrationTaskName taskName;

        public CreateProfileTask(String profileName) {
            this.profileName = profileName;
            this.taskName = new ServerMigrationTaskName.Builder("create-profile").addAttribute("name", profileName).build();
        }

        @Override
        public ServerMigrationTask getTask(final S source, final HostControllerConfiguration configuration) throws Exception {
            return new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return taskName;
                }

                @Override
                public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                    final PathAddress pathAddress = configuration.getProfilesManagement().getResourcePathAddress(profileName);
                    final ModelNode op = Util.createAddOperation(pathAddress);
                    configuration.executeManagementOperation(op);
                    context.getLogger().infof("Profile %s created.", profileName);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
        }
    }

    public static class Builder<S> extends ParentManageableServerConfigurationTaskFactory.Builder<S, HostControllerConfiguration>{

        private final String profileName;

        public Builder(final String profileName) {
            super(new ServerMigrationTaskName.Builder("add-profile-"+profileName).build());
            this.profileName = profileName;
            eventListener(new ParentServerMigrationTask.EventListener() {
                @Override
                public void started(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Adding profile %s...", profileName);
                }
                @Override
                public void done(ServerMigrationTaskContext context) {
                    context.getLogger().infof("Profile %s added.", profileName);
                }
            });
            skipTaskPropertyName(getTaskName().getName()+".skip");
            subtask(new CreateProfileTask(profileName));
        }

        @Override
        public Builder<S> eventListener(ParentServerMigrationTask.EventListener eventListener) {
            return (Builder<S>) super.eventListener(eventListener);
        }

        @Override
        public Builder<S> skipTaskPropertyName(String skipTaskPropertyName) {
            return (Builder<S>) super.skipTaskPropertyName(skipTaskPropertyName);
        }

        public Builder<S> subtask(final AddSubsystemTaskFactory<S> subtaskFactory) {
            return subtask(new ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
                    final ProfileManagement profileManagement = configuration.getProfilesManagement().getProfileManagement(profileName);
                    if (profileManagement == null) {
                        return null;
                    }
                    return subtaskFactory.getTask(source, profileManagement.getSubsystemsManagement());
                }
            });
        }

        @Override
        public Builder<S> subtask(ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> subtaskFactory) {
            return (Builder<S>) super.subtask(subtaskFactory);
        }

        public Builder<S> subtask(final DomainConfigurationTaskFactory<S> subtaskFactory) {
            return subtask(new ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
                    return subtaskFactory.getTask(source, configuration);
                }
            });
        }

        public AddProfileTaskFactory<S> build() {
            return new AddProfileTaskFactory(this);
        }
    }
}
