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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemConfigurationTask;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationSubtask;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds Undertow's default buffer cache.
 * @author emmartins
 */
public class AddBufferCache<S> extends UpdateSubsystemConfigurationSubtask<S> {

    public static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder("add-undertow-default-buffer-cache").build();

    private static final String BUFFER_CACHE = "buffer-cache";
    private static final String BUFFER_CACHE_NAME = "default";

    @Override
    public ServerMigrationTaskName getName(SubsystemConfigurationTask.Context<S> parentContext) {
        return TASK_NAME;
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        if (!config.hasDefined(BUFFER_CACHE, BUFFER_CACHE_NAME)) {
            final PathAddress pathAddress = parentContext.getSubsystemConfigurationPathAddress().append(BUFFER_CACHE, BUFFER_CACHE_NAME);
            parentContext.getServerConfiguration().executeManagementOperation(Util.createEmptyOperation(ADD, pathAddress));
            taskContext.getLogger().infof("Undertow's default buffer cache added to config %s.", parentContext.getSubsystemConfigurationName());
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
