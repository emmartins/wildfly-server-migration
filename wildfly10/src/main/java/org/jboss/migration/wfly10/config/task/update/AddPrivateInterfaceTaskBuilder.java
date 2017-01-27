/*
 * Copyright 2017 Red Hat, Inc.
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
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.ComponentTask;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.task.management.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.component.BasicManageableResourceComponentTask;
import org.jboss.migration.wfly10.config.task.management.socketbinding.SocketBindingGroupResourceCompositeTask;
import org.jboss.migration.wfly10.config.task.management.socketbinding.SocketBindingGroupResourceLeafTask;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.migration.wfly10.config.management.ManageableResourceSelectors.selectResources;
import static org.jboss.migration.wfly10.config.management.ManageableResourceSelectors.selectServerConfiguration;

/**
 * Adds private interface to config, and updates jgroup socket bindings to use it.
 * @author emmartins
 */
public class AddPrivateInterfaceTaskBuilder<S> extends ManageableServerConfigurationCompositeTask.Builder<S, ManageableServerConfiguration> {

    public static final AddPrivateInterfaceTaskBuilder INSTANCE = new AddPrivateInterfaceTaskBuilder();

    private static final String TASK_NAME = "setup-private-interface";
    private static final String INTERFACE_NAME = "private";
    private static final String[] SOCKET_BINDING_NAMES = {"jgroups-mping", "jgroups-tcp", "jgroups-tcp-fd", "jgroups-udp", "jgroups-udp-fd"};

    private AddPrivateInterfaceTaskBuilder() {
        super(new ServerMigrationTaskName.Builder(TASK_NAME).build(), ManageableServerConfiguration.class);
        beforeRun((task, context) -> context.getLogger().infof("Private interface setup starting..."));
        afterRun((task, context) -> context.getLogger().infof("Private interface setup done."));
        skipPolicy((task, context) -> {
            if (ComponentTask.Skippers.skipIfDefaultSkipPropertyIsSet().isSkipped(task, context)) {
                return true;
            }
            /*
            for (ManageableServerConfiguration resource : task.getResources()) {
                for (String socketBindingName : SOCKET_BINDING_NAMES) {
                    if (!resource.findResources(SocketBindingResource.class, socketBindingName).isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
            */
            for (String socketBindingName : SOCKET_BINDING_NAMES) {
                if (!selectServerConfiguration().andThen(selectResources(SocketBindingResource.class, socketBindingName)).fromResources(task.getResources()).isEmpty()) {
                    return false;
                }
            }
            return true;
        });
        subtask(new AddInterface<>());
        subtask(new UpdateSocketBindingGroups<>());
    }

    static class AddInterface<S> extends BasicManageableResourceComponentTask.Builder<S, ManageableServerConfiguration> {
        AddInterface() {
            super(new ServerMigrationTaskName.Builder("add-interface").build(), (task, context) -> {
                final ManageableServerConfiguration serverConfiguration = task.getResource();
                if (serverConfiguration.getInterfaceResourceNames().contains(INTERFACE_NAME)) {
                    context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final ModelNode addInterfaceOp = Util.createAddOperation(serverConfiguration.getInterfaceResourcePathAddress(INTERFACE_NAME));
                addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                serverConfiguration.executeManagementOperation(addInterfaceOp);
                context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            });
        }
    }

    static class UpdateSocketBindingGroups<S> extends SocketBindingGroupResourceCompositeTask.Builder<S> {
        public UpdateSocketBindingGroups() {
            super(new ServerMigrationTaskName.Builder("update-socket-binding-groups").build());
            subtask((fSource, fSocketBindingGroupResource, fContext) -> {
                final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("update-socket-binding-group").addAttribute("name", fSocketBindingGroupResource.getResourceName()).build();
                final SocketBindingGroupResourceLeafTask.Runnable<S> taskRunnable = (rSource, rSocketBindingGroupResource, rContext) -> {
                    final List<String> updated = new ArrayList<>();
                    for (String socketBinding : SOCKET_BINDING_NAMES) {
                        SocketBindingResource socketBindingResource = rSocketBindingGroupResource.getSocketBindingResource(socketBinding);
                        ModelNode config = socketBindingResource.getResourceConfiguration();
                        if (config != null) {
                            if (!config.hasDefined(INTERFACE) || !config.get(INTERFACE).asString().equals(INTERFACE_NAME)) {
                                final PathAddress pathAddress = socketBindingResource.getResourcePathAddress();
                                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                                writeAttrOp.get(NAME).set(INTERFACE);
                                writeAttrOp.get(VALUE).set(INTERFACE_NAME);
                                socketBindingResource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                                rContext.getLogger().infof("Socket binding %s interface set to %s", pathAddress.toCLIStyleString(), INTERFACE_NAME);
                                updated.add(socketBinding);
                            }
                        }
                    }
                    if (updated.isEmpty()) {
                        return ServerMigrationTaskResult.SKIPPED;
                    } else {
                        return new ServerMigrationTaskResult.Builder().success().addAttribute("updated", updated.toString()).build();
                    }
                };
                return new SocketBindingGroupResourceLeafTask.Builder<>(taskName, taskRunnable).build(fSource, fSocketBindingGroupResource);
            });
        }
    }
}