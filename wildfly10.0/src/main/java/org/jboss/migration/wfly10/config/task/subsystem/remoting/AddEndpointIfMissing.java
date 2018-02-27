/*
 * Copyright 2018 Red Hat, Inc.
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
package org.jboss.migration.wfly10.config.task.subsystem.remoting;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds the remoting endpoint.
 * @author emmartins
 */
public class AddEndpointIfMissing<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String CONFIGURATION = "configuration";
    public static final String ENDPOINT = "endpoint";

    public static final String TASK_NAME = "add-remoting-endpoint";

    public AddEndpointIfMissing() {
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext taskContext, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration serverConfiguration = subsystemResource.getServerConfiguration();
        // if not defined add endpoint connector
        if (!config.hasDefined(CONFIGURATION, ENDPOINT)) {
            final PathAddress pathAddress = subsystemPathAddress.append(CONFIGURATION, ENDPOINT);
            final ModelNode op = Util.createEmptyOperation(ADD, pathAddress);
            serverConfiguration.executeManagementOperation(op);
            taskContext.getLogger().debugf("Endpoint added to Remoting subsystem configuration.");
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
