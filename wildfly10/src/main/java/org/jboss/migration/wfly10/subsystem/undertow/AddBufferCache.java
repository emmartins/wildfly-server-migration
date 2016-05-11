/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds Undertow's default buffer cache.
 * @author emmartins
 */
public class AddBufferCache implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddBufferCache INSTANCE = new AddBufferCache();

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("add-undertow-default-buffer-cache").build();

    private AddBufferCache() {
    }

    private static final String BUFFER_CACHE = "buffer-cache";
    private static final String BUFFER_CACHE_NAME = "default";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (!config.hasDefined(BUFFER_CACHE, BUFFER_CACHE_NAME)) {
                    final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), PathElement.pathElement(BUFFER_CACHE, BUFFER_CACHE_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    server.executeManagementOperation(addOp);
                    context.getLogger().infof("Undertow's default buffer cache added.");
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
