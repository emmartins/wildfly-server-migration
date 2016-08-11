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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration of a domain profile config.
 *  @author emmartins
 */
public class ProfileMigration<S> implements ProfilesMigration.SubtaskFactory<S> {

    public static final String PROFILE = "profile";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTR_NAME = "name";

    protected final List<SubtaskFactory<S>> subtaskFactories;

    protected ProfileMigration(Builder builder) {
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    @Override
    public void addSubtasks(S source, ProfilesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, final S source, final ProfilesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(PROFILE)
                .addAttribute(SERVER_MIGRATION_TASK_NAME_ATTR_NAME, resourceName)
                .build();
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(SERVER_MIGRATION_TASK_NAME)
                .eventListener(new ParentServerMigrationTask.EventListener() {
                                   @Override
                                   public void started(ServerMigrationTaskContext context) {
                                       context.getLogger().infof("Profile %s migration starting...", resourceName);
                                   }
                                   @Override
                                   public void done(ServerMigrationTaskContext context) {
                                       context.getLogger().infof("Profile %s migration done.", resourceName);
                                   }
                               })
                .succeedOnlyIfHasSuccessfulSubtasks();
        final ProfileManagement profileManagement = resourceManagement.getProfileManagement(resourceName);
        final ServerMigrationTasks serverMigrationTasks = taskBuilder.getSubtasks();
        for (SubtaskFactory<S> subtaskFactory : subtaskFactories) {
            subtaskFactory.addSubtasks(source, profileManagement, serverMigrationTasks);
        }
        subtasks.add(taskBuilder.build());
    }

    public interface SubtaskFactory<S> {
        void addSubtasks(S source, ProfileManagement profileManagement, ServerMigrationTasks subtasks) throws Exception;
    }

    public static class Builder<S> {
        private final List<SubtaskFactory<S>> subtaskFactories = new ArrayList<>();

        public Builder addSubtaskFactory(SubtaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public Builder addSubsystemsMigration(final SubsystemsMigration<S> subsystemsMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, ProfileManagement profileManagement, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = subsystemsMigration.getSubsystemsManagementTask(source, profileManagement.getSubsystemsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public ProfileMigration build() {
            return new ProfileMigration(this);
        }
    }
}