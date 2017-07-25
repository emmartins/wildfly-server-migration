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
package org.jboss.migration.eap.task.subsystem.logging;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

/**
 * A task which removes the logging's console handler.
 * @author emmartins
 */
public class RemoveConsoleHandler<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "remove-console-handler";
    private static final String CONSOLE_HANDLER = "console-handler";
    private static final String CONSOLE = "CONSOLE";
    private static final String REMOVE_HANDLER = "remove-handler";
    private static final String ROOT_LOGGER = "root-logger";
    private static final String ROOT = "ROOT";
    private static final String NAME = "name";

    public RemoveConsoleHandler() {
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        if (!config.hasDefined(CONSOLE_HANDLER, CONSOLE)) {
            context.getLogger().infof("Console handler not found, skipping.");
            return ServerMigrationTaskResult.SKIPPED;
        }
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        compositeOperationBuilder.addStep(Util.createEmptyOperation(REMOVE, subsystemResource.getResourcePathAddress().append(CONSOLE_HANDLER, CONSOLE)));
        final ModelNode removeHandlerOp = Util.createEmptyOperation(REMOVE_HANDLER, subsystemResource.getResourcePathAddress().append(ROOT_LOGGER, ROOT));
        removeHandlerOp.get(NAME).set(CONSOLE);
        compositeOperationBuilder.addStep(removeHandlerOp);
        subsystemResource.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());
        context.getLogger().infof("Console handler removed.");
        return ServerMigrationTaskResult.SUCCESS;
    }
}
