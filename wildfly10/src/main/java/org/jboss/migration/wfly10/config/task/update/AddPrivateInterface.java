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
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.task.executor.InterfacesManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SocketBindingGroupsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Adds private interface to config, and updates jgroup socket bindings to use it.
 * @author emmartins
 */
public class AddPrivateInterface<S> implements ManageableServerConfigurationTaskFactory<S, ManageableServerConfiguration> {

    private static final String INTERFACE_NAME = "private";
    private static final String[] SOCKET_BINDING_NAMES = {"jgroups-mping", "jgroups-tcp", "jgroups-tcp-fd", "jgroups-udp", "jgroups-udp-fd"};
    private static final String TASK_NAME = "setup-private-interface";

    public static final AddPrivateInterface INSTANCE = new AddPrivateInterface();

    private AddPrivateInterface() {
    }

    @Override
    public ServerMigrationTask getTask(S source, ManageableServerConfiguration configuration) throws Exception {
        return new ParentServerMigrationTask.Builder(new ServerMigrationTaskName.Builder(TASK_NAME).build())
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {
                        context.getLogger().infof("Private interface setup starting...");
                    }
                    @Override
                    public void done(TaskContext context) {
                        context.getLogger().infof("Private interface setup done.");
                    }
                })
                .subtask(SubtaskExecutorAdapters.of(source, configuration, new AddInterface()))
                .subtask(SubtaskExecutorAdapters.of(source, configuration, new UpdateSocketBindingGroups()))
                .build();
    }

    static class AddInterface<S> implements InterfacesManagementSubtaskExecutor<S> {

        private static final ServerMigrationTaskName SUBTASK_NAME = new ServerMigrationTaskName.Builder("add-interface").build();

        @Override
        public void executeSubtasks(S source, final InterfacesManagement interfacesManagement, TaskContext context) throws Exception {
            // subtask to add private interface
            final ServerMigrationTask task = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SUBTASK_NAME;
                }

                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    if (interfacesManagement.getResourceNames().contains(INTERFACE_NAME)) {
                        context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    boolean addInterface = false;
                    final SocketBindingGroupsManagement socketBindingGroupsManagement = interfacesManagement.getServerConfiguration().getSocketBindingGroupsManagement();
                    for (String socketBindingGroupName : socketBindingGroupsManagement.getResourceNames()) {
                        final SocketBindingGroupManagement socketBindingGroupManagement = socketBindingGroupsManagement.getSocketBindingGroupManagement(socketBindingGroupName);
                        final Set<String> socketBindings = socketBindingGroupManagement.getSocketBindingsManagement().getResourceNames();
                        for (String jgroupsSocketBinding : SOCKET_BINDING_NAMES) {
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
                        context.getLogger().debugf("Skipping task to add private interface, the target socket bindings are not present in the configuration.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    final ModelNode addInterfaceOp = Util.createAddOperation(interfacesManagement.getResourcePathAddress(INTERFACE_NAME));
                    addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                    interfacesManagement.getServerConfiguration().executeManagementOperation(addInterfaceOp);
                    context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            context.execute(new SkippableByEnvServerMigrationTask(task, TASK_NAME + "." + SUBTASK_NAME + ".skip"));
        }
    }

    static class UpdateSocketBindingGroups<S> implements SocketBindingGroupsManagementSubtaskExecutor<S> {

        private static final ServerMigrationTaskName SUBTASK_NAME = new ServerMigrationTaskName.Builder("update-socket-binding-groups").build();

        @Override
        public void executeSubtasks(S source, SocketBindingGroupsManagement socketBindingGroupsManagement, TaskContext context) throws Exception {
            final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(SUBTASK_NAME)
                    .skipTaskPropertyName(TASK_NAME + "." + SUBTASK_NAME + ".skip");
            for (final String socketBindingGroupName : socketBindingGroupsManagement.getResourceNames()) {
                final ServerMigrationTask subtask = getResourceTask(source, socketBindingGroupsManagement.getSocketBindingGroupManagement(socketBindingGroupName));
                if (subtask != null) {
                    taskBuilder.subtask(subtask);
                }
            }
            context.execute(taskBuilder.build());
        }

        public ServerMigrationTask getResourceTask(S source, final SocketBindingGroupManagement socketBindingGroupManagement) throws Exception {
            // subtask to update jgroup socket bindings, to use private interface
            final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("update-socket-binding-group").addAttribute("name", socketBindingGroupManagement.getSocketBindingGroupName()).build();
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return subtaskName;
                }

                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    final List<String> updated = new ArrayList<>();
                    final SocketBindingsManagement resourceManagement = socketBindingGroupManagement.getSocketBindingsManagement();
                    for (String socketBinding : SOCKET_BINDING_NAMES) {
                        ModelNode config = resourceManagement.getResourceConfiguration(socketBinding);
                        if (config != null) {
                            if (!config.hasDefined(INTERFACE) || !config.get(INTERFACE).asString().equals(INTERFACE_NAME)) {
                                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(socketBinding);
                                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                                writeAttrOp.get(NAME).set(INTERFACE);
                                writeAttrOp.get(VALUE).set(INTERFACE_NAME);
                                resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                                context.getLogger().infof("Socket binding %s interface set to %s", pathAddress.toCLIStyleString(), INTERFACE_NAME);
                                updated.add(socketBinding);
                            }
                        }
                    }
                    if (updated.isEmpty()) {
                        return ServerMigrationTaskResult.SKIPPED;
                    } else {
                        return new ServerMigrationTaskResult.Builder()
                                .sucess()
                                .addAttribute("updated", updated.toString())
                                .build();
                    }
                }
            };
            return subtask;
        }
    }
}