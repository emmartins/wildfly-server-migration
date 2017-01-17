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

package org.jboss.migration.wfly10.config.task.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.UpdateSubsystemTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;

/**
 * A task which adds Undertow's default https listener.
 * @author emmartins
 */
public class AddHttpsListener implements UpdateSubsystemTaskFactory.SubtaskFactory {

    public static final AddHttpsListener INSTANCE = new AddHttpsListener();

    public static final String TASK_NAME_NAME = "add-undertow-https-listener";
    public static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();

    private static final String SERVER_NAME = "default-server";
    private static final String HTTPS_LISTENER = "https-listener";
    private static final String HTTPS_LISTENER_NAME = "https";

    private AddHttpsListener() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement) {
        return new UpdateSubsystemTaskFactory.Subtask(config, subsystem, subsystemsManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return TASK_NAME;
            }

            @Override
            protected ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                // refresh subsystem config to see any changes possibly made during migration
                config = subsystemsManagement.getResourceConfiguration(subsystem.getName());
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress configPathAddress = subsystemsManagement.getResourcePathAddress(subsystem.getName());
                final PathAddress serverPathAddress = configPathAddress.append(PathElement.pathElement(SERVER, SERVER_NAME));
                if (!config.hasDefined(SERVER, SERVER_NAME)) {
                    context.getLogger().debugf("Skipping task, server '%s' not found in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final ModelNode server = config.get(SERVER, SERVER_NAME);
                if (server.hasDefined(HTTPS_LISTENER, HTTPS_LISTENER_NAME)) {
                    context.getLogger().debugf("Skipping task, https listener already defined by server '%s', in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                /*
                add to server's config
                "https-listener" => {
                    "https" => {
                        "security-realm" => "ApplicationRealm",
                        "socket-binding" => "https"
                    }
                }
                */
                final PathAddress httpsListenerPathAddress = serverPathAddress.append(PathElement.pathElement(HTTPS_LISTENER, HTTPS_LISTENER_NAME));
                final ModelNode op = Util.createAddOperation(httpsListenerPathAddress);
                op.get(SOCKET_BINDING).set("https");
                op.get(SECURITY_REALM).set("ApplicationRealm");
                subsystemsManagement.getServerConfiguration().executeManagementOperation(op);
                context.getLogger().infof("Default HTTPS listener added to server '%s', in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
