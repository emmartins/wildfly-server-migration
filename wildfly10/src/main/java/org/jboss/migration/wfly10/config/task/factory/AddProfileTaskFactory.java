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
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemTaskFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class AddProfileTaskFactory<S> implements DomainConfigurationTaskFactory<S> {

    private final String profileName;
    private final ServerMigrationTaskName taskName;
    private final List<ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>> subtasks;
    private final String skipTaskPropertyName;
    private final AbstractServerMigrationTask.Listener eventListener;

    protected AddProfileTaskFactory(Builder<S> builder) {
        this.profileName = builder.profileName;
        this.taskName = builder.taskName != null ? builder.taskName : new ServerMigrationTaskName.Builder("add-profile-"+profileName).build();
        this.subtasks = Collections.unmodifiableList(builder.subtasks);
        this.skipTaskPropertyName = builder.skipTaskPropertyName != null ? builder.skipTaskPropertyName : (taskName.getName()+".skip");
        this.eventListener = builder.eventListener != null ? builder.eventListener : new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding profile %s...", profileName);
            }
            @Override
            public void done(TaskContext context) {
                context.getLogger().infof("Profile %s added.", profileName);
            }
        };
    }

    @Override
    public ServerMigrationTask getTask(final S source, final HostControllerConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .listener(eventListener)
                .subtask(new CreateProfileTask(profileName, configuration));
        for (ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> subtaskFactory : subtasks) {
            final ServerMigrationTask subtask = subtaskFactory.getTask(source, configuration);
            if (subtask != null) {
                taskBuilder.subtask(subtask);
            }
        }
        return taskBuilder.build();
    }

    public static class CreateProfileTask implements ServerMigrationTask {
        private final HostControllerConfiguration configuration;
        private final String profileName;
        private final ServerMigrationTaskName taskName;

        public CreateProfileTask(String profileName, HostControllerConfiguration configuration) {
            this.configuration = configuration;
            this.profileName = profileName;
            this.taskName = new ServerMigrationTaskName.Builder("create-profile").addAttribute("name", profileName).build();
        }

        @Override
        public ServerMigrationTaskName getName() {
            return taskName;
        }

        @Override
        public ServerMigrationTaskResult run(TaskContext context) throws Exception {
            final PathAddress pathAddress = configuration.getProfileResources().getResourcePathAddress(profileName);
            final ModelNode op = Util.createAddOperation(pathAddress);
            configuration.executeManagementOperation(op);
            context.getLogger().infof("Profile %s created.", profileName);
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    public static class Builder<S> {

        private ServerMigrationTaskName taskName;
        private final String profileName;
        private final List<ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>> subtasks;
        private String skipTaskPropertyName;
        private AbstractServerMigrationTask.Listener eventListener;

        public Builder(String profileName) {
            this.profileName = profileName;
            this.subtasks = new ArrayList<>();
        }

        public Builder<S> eventListener(AbstractServerMigrationTask.Listener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder<S> skipTaskPropertyName(String skipTaskPropertyName) {
            this.skipTaskPropertyName = skipTaskPropertyName;
            return this;
        }

        public Builder<S> subtask(final AddSubsystemTaskFactory<S> subtaskFactory) {
            return subtask(new ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
                    final ProfileResource profileResource = configuration.getProfileResources().getResource(profileName);
                    if (profileResource == null) {
                        return null;
                    }
                    return subtaskFactory.getTask(source, profileResource.getSubsystemsManagement());
                }
            });
        }

        public Builder<S> subtask(ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> subtaskFactory) {
            subtasks.add(subtaskFactory);
            return this;
        }

        public Builder<S> subtask(final DomainConfigurationTaskFactory<S> subtaskFactory) {
            return subtask(new ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
                    return subtaskFactory.getTask(source, configuration);
                }
            });
        }

        public Builder<S> taskName(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
            return this;
        }

        public AddProfileTaskFactory<S> build() {
            return new AddProfileTaskFactory(this);
        }
    }
}
