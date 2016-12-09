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

package org.jboss.migration.eap.task;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class AddDomainProfileTaskFactory<S> implements DomainConfigurationTaskFactory<S> {

    private final String profileName;
    private final ServerMigrationTaskName taskName;
    private final List<SubsystemsManagementSubtaskExecutor> subtasks;
    private final String skipTaskPropertyName;
    private final ParentServerMigrationTask.EventListener eventListener;

    protected AddDomainProfileTaskFactory(Builder<S> builder) {
        this.profileName = builder.profileName;
        this.taskName = builder.taskName != null ? builder.taskName : new ServerMigrationTaskName.Builder("add-domain-profile-"+profileName).build();
        this.subtasks = Collections.unmodifiableList(builder.subtasks);
        this.skipTaskPropertyName = builder.skipTaskPropertyName != null ? builder.skipTaskPropertyName : (taskName.getName()+".skip");
        this.eventListener = builder.eventListener != null ? builder.eventListener : new ParentServerMigrationTask.EventListener() {
            @Override
            public void started(ServerMigrationTaskContext context) {
                context.getLogger().infof("Adding domain profile %s...", profileName);
            }

            @Override
            public void done(ServerMigrationTaskContext context) {
                context.getLogger().infof("Domain profile %s added.", profileName);
            }
        };
    }

    @Override
    public ServerMigrationTask getTask(final S source, final HostControllerConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .eventListener(eventListener)
                .subtask(new CreateProfileTask(profileName, configuration));
        for (final SubsystemsManagementSubtaskExecutor<S> subtask : subtasks) {
            taskBuilder.subtask(new ParentServerMigrationTask.SubtaskExecutor() {
                @Override
                public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                   final ProfileManagement profileManagement = configuration.getProfilesManagement().getProfileManagement(profileName);
                   subtask.executeSubtasks(source, profileManagement.getSubsystemsManagement(), context);
                }
            });
        }
        final ServerMigrationTask task = taskBuilder.build();


        return new SkippableByEnvServerMigrationTask(taskBuilder.build(), skipTaskPropertyName);
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
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            final PathAddress pathAddress = configuration.getProfilesManagement().getResourcePathAddress(profileName);
            final ModelNode op = Util.createAddOperation(pathAddress);
            configuration.executeManagementOperation(op);
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    public static class Builder<S> {

        private ServerMigrationTaskName taskName;
        private final String profileName;
        private final List<SubsystemsManagementSubtaskExecutor> subtasks;
        private String skipTaskPropertyName;
        private ParentServerMigrationTask.EventListener eventListener;

        public Builder(String profileName) {
            this.profileName = profileName;
            this.subtasks = new ArrayList<>();
        }

        public Builder<S> eventListener(ParentServerMigrationTask.EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder<S> skipTaskPropertyName(String skipTaskPropertyName) {
            this.skipTaskPropertyName = skipTaskPropertyName;
            return this;
        }

        public Builder<S> subtask(SubsystemsManagementSubtaskExecutor<S> subtask) {
            subtasks.add(subtask);
            return this;
        }

        public Builder<S> taskName(ServerMigrationTaskName taskName) {
            this.taskName = taskName;
            return this;
        }

        public AddDomainProfileTaskFactory<S> build() {
            return new AddDomainProfileTaskFactory(this);
        }
    }
}
