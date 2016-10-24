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
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.task.ManagementInterfacesMigration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Updates the management interface http-interface to support http upgrade.
 * @author emmartins
 */
public class SetManagementInterfacesHttpUpgradeEnabled implements ManagementInterfacesMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "set-management-interfaces-http-upgrade-enabled";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
    private static final String MANAGEMENT_INTERFACE_NAME = "http-interface";

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, final ManagementInterfacesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTask subtask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                // retrieve resource config
                final ModelNode resource = resourceManagement.getResource(MANAGEMENT_INTERFACE_NAME);
                if (resource == null) {
                    context.getLogger().debugf("Management interface %s does not exists.", MANAGEMENT_INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // check if attribute is defined
                if (resource.hasDefined(HTTP_UPGRADE_ENABLED) && resource.get(HTTP_UPGRADE_ENABLED).asBoolean()) {
                    context.getLogger().debugf("Management interface %s http upgrade already enabled.", MANAGEMENT_INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // set attribute value
                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(MANAGEMENT_INTERFACE_NAME);
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(HTTP_UPGRADE_ENABLED);
                writeAttrOp.get(VALUE).set(true);
                resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().debugf("Management interface '%s' http upgrade enabled.", MANAGEMENT_INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        subtasks.add(new SkippableByEnvServerMigrationTask(subtask, ManagementInterfacesMigration.MANAGEMENT_INTERFACES+"."+SERVER_MIGRATION_TASK_NAME_NAME+".skip"));
    }
}