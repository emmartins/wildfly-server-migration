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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.task.InterfacesMigration;

/**
 * Adds private interface to config.
 * @author emmartins
 */
public class RemoveUnsecureInterface implements InterfacesMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "remove-unsecure-interface";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
    private static final String INTERFACE_NAME = "unsecure";

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, final InterfacesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTask subtask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                // retrieve resource config
                if (!resourceManagement.getResourceNames().contains(INTERFACE_NAME)) {
                    context.getLogger().debugf("Interface %s does not exists.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(INTERFACE_NAME);
                final ModelNode removeOp = Util.createRemoveOperation(pathAddress);
                resourceManagement.getServerConfiguration().executeManagementOperation(removeOp);
                context.getLogger().infof("Interface %s removed.", INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        subtasks.add(new SkippableByEnvServerMigrationTask(subtask, InterfacesMigration.INTERFACES+"."+SERVER_MIGRATION_TASK_NAME_NAME+".skip"));
    }
}