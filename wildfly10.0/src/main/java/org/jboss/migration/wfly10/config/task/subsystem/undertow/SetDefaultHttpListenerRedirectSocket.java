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
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;

/**
 * A task which ensures that Undertow's default http listener 'redirect-socket' is set.
 * @author emmartins
 */
public class SetDefaultHttpListenerRedirectSocket<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "set-default-http-listener-redirect-socket";
    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String REDIRECT_SOCKET_ATTR_NAME = "redirect-socket";
    private static final String REDIRECT_SOCKET_ATTR_VALUE = "https";

    private final String httpListenerName;

    public SetDefaultHttpListenerRedirectSocket() {
        this("default");
    }

    public SetDefaultHttpListenerRedirectSocket(String defaultHttpListenerName) {
        this.httpListenerName = defaultHttpListenerName;
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        if (config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, httpListenerName) && !config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, httpListenerName, REDIRECT_SOCKET_ATTR_NAME)) {
            final PathAddress pathAddress = subsystemResource.getResourcePathAddress().append(SERVER, SERVER_NAME).append(HTTP_LISTENER, httpListenerName);
            final ModelNode op = Util.getWriteAttributeOperation(pathAddress, REDIRECT_SOCKET_ATTR_NAME, REDIRECT_SOCKET_ATTR_VALUE);
            subsystemResource.getServerConfiguration().executeManagementOperation(op);
            context.getLogger().infof("Undertow's default HTTP listener '%s' redirect-socket set as 'https'.", httpListenerName);
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
