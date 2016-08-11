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

package org.jboss.migration.eap6.to.eap7.interfaces;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.eap6.to.eap7.socketbindings.UsePrivateInterfaceOnJGroupsSocketBindings;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.task.InterfacesMigration;

import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;

/**
 * Adds private interface to config.
 * @author emmartins
 */
public class AddPrivateInterface implements InterfacesMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "add-private-interface";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
    private static final String INTERFACE_NAME = "private";

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, final InterfacesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTask subtask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (resourceManagement.getResourceNames().contains(INTERFACE_NAME)) {
                    context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                boolean addInterface = false;
                final SocketBindingGroupsManagement socketBindingGroupsManagement = resourceManagement.getServerConfiguration().getSocketBindingGroupsManagement();
                for (String socketBindingGroupName : socketBindingGroupsManagement.getResourceNames()) {
                    final SocketBindingGroupManagement socketBindingGroupManagement = socketBindingGroupsManagement.getSocketBindingGroupManagement(socketBindingGroupName);
                    final Set<String> socketBindings = socketBindingGroupManagement.getSocketBindingsManagement().getResourceNames();
                    for (String jgroupsSocketBinding : UsePrivateInterfaceOnJGroupsSocketBindings.JGROUPS_SOCKET_BINDINGS) {
                        if (socketBindings.contains(jgroupsSocketBinding)) {
                            addInterface = true;
                            break;
                        }
                    }
                    if (addInterface) {
                        break;
                    }
                }
                if (!addInterface) {
                    context.getLogger().debugf("Skipping task to add private interface, the target soket bindings are not present in the configuration.");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final ModelNode addInterfaceOp = Util.createAddOperation(resourceManagement.getResourcePathAddress(INTERFACE_NAME));
                addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                resourceManagement.getServerConfiguration().executeManagementOperation(addInterfaceOp);
                context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        subtasks.add(subtask);
    }
}