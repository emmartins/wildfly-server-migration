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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.JBossServer;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.task.InterfacesMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingsMigration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Adds private interface to config, and updates jgroup socket bindings to use it.
 * @author emmartins
 */
public class AddPrivateInterface {

    private static final String INTERFACE_ATTR_VALUE = "private";
    private static final String[] JGROUPS_SOCKET_BINDINGS = {"jgroups-mping", "jgroups-tcp", "jgroups-tcp-fd", "jgroups-udp", "jgroups-udp-fd"};

    public static class InterfacesSubtaskFactory<S extends JBossServer> implements InterfacesMigration.SubtaskFactory<ServerPath<S>> {

        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("add-private-interface").build();

        @Override
        public void addSubtasks(ServerPath<S> source, final InterfacesManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
            // subtask to add private interface
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SERVER_MIGRATION_TASK_NAME;
                }
                @Override
                public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                    if (resourceManagement.getResourceNames().contains(INTERFACE_ATTR_VALUE)) {
                        context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    boolean addInterface = false;
                    final SocketBindingGroupsManagement socketBindingGroupsManagement = resourceManagement.getServerConfiguration().getSocketBindingGroupsManagement();
                    for (String socketBindingGroupName : socketBindingGroupsManagement.getResourceNames()) {
                        final SocketBindingGroupManagement socketBindingGroupManagement = socketBindingGroupsManagement.getSocketBindingGroupManagement(socketBindingGroupName);
                        final Set<String> socketBindings = socketBindingGroupManagement.getSocketBindingsManagement().getResourceNames();
                        for (String jgroupsSocketBinding : JGROUPS_SOCKET_BINDINGS) {
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
                    final ModelNode addInterfaceOp = Util.createAddOperation(resourceManagement.getResourcePathAddress(INTERFACE_ATTR_VALUE));
                    addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                    resourceManagement.getServerConfiguration().executeManagementOperation(addInterfaceOp);
                    context.getLogger().infof("Interface %s added.", INTERFACE_ATTR_VALUE);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            subtasks.add(subtask);
        }
    }

    public static class SocketBindingsSubtaskFactory<S extends JBossServer> implements SocketBindingsMigration.SubtaskFactory<ServerPath<S>> {

        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("set-jgroups-socket-bindings-interface-private").build();

        @Override
        public void addSubtasks(ServerPath<S> source, final SocketBindingsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
            // subtask to update jgroup socket bindings, to use private interface
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SERVER_MIGRATION_TASK_NAME;
                }

                @Override
                public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                    final List<String> updated = new ArrayList<>();
                    for (String socketBinding : JGROUPS_SOCKET_BINDINGS) {
                        ModelNode config = resourceManagement.getResource(socketBinding);
                        if (config != null) {
                            if (!config.hasDefined(INTERFACE) || !config.get(INTERFACE).asString().equals(INTERFACE_ATTR_VALUE)) {
                                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(socketBinding);
                                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                                writeAttrOp.get(NAME).set(INTERFACE);
                                writeAttrOp.get(VALUE).set(INTERFACE_ATTR_VALUE);
                                resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                                context.getLogger().infof("Socket binding %s interface set to %s", socketBinding, INTERFACE_ATTR_VALUE);
                                updated.add(socketBinding);
                            }
                        }
                    }
                    if (updated.isEmpty()) {
                        return ServerMigrationTaskResult.SKIPPED;
                    } else {
                        return new ServerMigrationTaskResult.Builder().sucess().addAttribute("updated", updated.toString()).build();
                    }
                }
            };
            subtasks.add(subtask);
        }
    }
}