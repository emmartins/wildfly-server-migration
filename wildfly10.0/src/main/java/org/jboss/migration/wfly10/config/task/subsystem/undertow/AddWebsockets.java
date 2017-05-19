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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds support for websockets.
 * @author emmartins
 */
public class AddWebsockets<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "add-undertow-websockets";
    private static final String SERVLET_CONTAINER = "servlet-container";
    private static final String SERVLET_CONTAINER_NAME = "default";
    private static final String SETTING = "setting";
    private static final String SETTING_NAME = "websockets";

    public AddWebsockets() {
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        if (!config.hasDefined(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME, SETTING, SETTING_NAME)) {
            final PathAddress pathAddress = subsystemResource.getResourcePathAddress().append(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME).append(SETTING, SETTING_NAME);
            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
            subsystemResource.getServerConfiguration().executeManagementOperation(addOp);
            context.getLogger().infof("Undertow's default Servlet Container configured to support Websockets.");
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
