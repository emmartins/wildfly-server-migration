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
import org.jboss.migration.wfly10.config.management.ServerGroupManagement;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration of a domain config's server group.
 *  @author emmartins
 */
public class ServerGroupMigration<S> implements ServerGroupsMigration.SubtaskFactory<S> {

    public static final String SERVER_GROUP = "server-group";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTR_NAME = "name";

    protected final List<SubtaskFactory<S>> subtaskFactories;

    protected ServerGroupMigration(Builder builder) {
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    @Override
    public void addSubtasks(S source, ServerGroupsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String serverGroupName, S source, ServerGroupsManagement serverGroupsManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_GROUP)
                .addAttribute(SERVER_MIGRATION_TASK_NAME_ATTR_NAME, serverGroupName)
                .build();
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(SERVER_MIGRATION_TASK_NAME)
                .eventListener(new ParentServerMigrationTask.EventListener() {
                    @Override
                    public void started(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Server group %s migration starting...", serverGroupName);
                    }
                    @Override
                    public void done(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Server group %s migration done.", serverGroupName);
                    }
                })
                .succeedOnlyIfHasSuccessfulSubtasks();
        final ServerMigrationTasks serverMigrationTasks = taskBuilder.getSubtasks();
        final ServerGroupManagement serverGroupManagement = serverGroupsManagement.getServerGroupManagement(serverGroupName);
        for (SubtaskFactory<S> subtaskFactory : subtaskFactories) {
            subtaskFactory.addSubtasks(source, serverGroupManagement, serverMigrationTasks);
        }
        subtasks.add(taskBuilder.build());
    }

    public interface SubtaskFactory<S> {
        void addSubtasks(S source, ServerGroupManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception;
    }

    public static class Builder<S> {
        private final List<SubtaskFactory<S>> subtaskFactories = new ArrayList<>();

        public Builder<S> addSubtaskFactory(SubtaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public Builder<S> jvmsMigration(final JVMsMigration<S> jvMsMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, ServerGroupManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subTask = jvMsMigration.getTask(source, resourceManagement.getJVMsManagement());
                    if (subTask != null) {
                        subtasks.add(subTask);
                    }
                }
            });
        }

        public Builder<S> jvmsMigration(final JVMsMigration.Builder<S> jvMsMigrationBuilder) {
            return jvmsMigration(jvMsMigrationBuilder.build());
        }

        public ServerGroupMigration<S> build() {
            return new ServerGroupMigration(this);
        }
    }
}