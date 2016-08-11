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
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration of a socket binding group.
 *  @author emmartins
 */
public class SocketBindingGroupMigration<S> implements SocketBindingGroupsMigration.SubtaskFactory<S> {

    public static final String SOCKET_BINDING = "socket-binding";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTR_NAME = "name";

    protected final List<SubtaskFactory<S>> subtaskFactories;

    protected SocketBindingGroupMigration(Builder builder) {
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    @Override
    public void addSubtasks(S source, SocketBindingGroupsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, S source, SocketBindingGroupsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SOCKET_BINDING)
                .addAttribute(SERVER_MIGRATION_TASK_NAME_ATTR_NAME, resourceName)
                .build();
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(SERVER_MIGRATION_TASK_NAME)
                .eventListener(new ParentServerMigrationTask.EventListener() {
                    @Override
                    public void started(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Socket binding group %s migration starting...", resourceName);
                    }
                    @Override
                    public void done(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Socket binding group %s migration done.", resourceName);
                    }
                })
                .succeedOnlyIfHasSuccessfulSubtasks();
        final ServerMigrationTasks serverMigrationTasks = taskBuilder.getSubtasks();
        final SocketBindingGroupManagement socketBindingGroupManagement = resourceManagement.getSocketBindingGroupManagement(resourceName);
        for (SubtaskFactory subtaskFactory : subtaskFactories) {
            subtaskFactory.addSubtasks(source, socketBindingGroupManagement, serverMigrationTasks);
        }
        subtasks.add(taskBuilder.build());
    }

    public interface SubtaskFactory<S> {
        void addSubtasks(S source, SocketBindingGroupManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception;
    }

    public static class Builder<S> {
        private final List<SubtaskFactory<S>> subtaskFactories = new ArrayList<>();

        public Builder addSubtaskFactory(SubtaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public Builder addSocketBindingsMigration(final SocketBindingsMigration<S> socketBindingsMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, SocketBindingGroupManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subTask = socketBindingsMigration.getTask(source, resourceManagement.getSocketBindingsManagement());
                    if (subTask != null) {
                        subtasks.add(subTask);
                    }
                }
            });
        }

        public Builder addSocketBindingsMigration(SocketBindingsMigration.Builder<S> socketBindingsMigrationBuilder) {
            return addSocketBindingsMigration(socketBindingsMigrationBuilder.build());
        }

        public SocketBindingGroupMigration build() {
            return new SocketBindingGroupMigration(this);
        }
    }
}