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
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.subsystem.UpdateSubsystemTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which ensures that Undertow's default http listener 'redirect-socket' is set.
 * @author emmartins
 */
public class SetDefaultHttpListenerRedirectSocket implements UpdateSubsystemTaskFactory.SubtaskFactory {

    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTP_LISTENER_NAME = "http";
    private static final String REDIRECT_SOCKET_ATTR_NAME = "redirect-socket";
    private static final String REDIRECT_SOCKET_ATTR_VALUE = "https";

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("set-default-http-listener-redirect-socket").build();

    public static final SetDefaultHttpListenerRedirectSocket INSTANCE = new SetDefaultHttpListenerRedirectSocket();

    private SetDefaultHttpListenerRedirectSocket() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemResources subsystemResources) {
        return new UpdateSubsystemTaskFactory.Subtask(config, subsystem, subsystemResources) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            protected ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemResources subsystemResources, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME) && !config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME, REDIRECT_SOCKET_ATTR_NAME)) {
                    final PathAddress pathAddress = subsystemResources.getResourcePathAddress(subsystem.getName()).append(PathElement.pathElement(SERVER, SERVER_NAME), PathElement.pathElement(HTTP_LISTENER, HTTP_LISTENER_NAME));
                    final ModelNode op = Util.getWriteAttributeOperation(pathAddress, REDIRECT_SOCKET_ATTR_NAME, REDIRECT_SOCKET_ATTR_VALUE);
                    subsystemResources.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().infof("Undertow's default HTTP listener 'redirect-socket' set as 'https'.");
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
