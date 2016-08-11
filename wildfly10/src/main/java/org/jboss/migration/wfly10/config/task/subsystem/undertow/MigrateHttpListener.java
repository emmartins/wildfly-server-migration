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
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.config.task.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * A task which updates the config of the default http listener.
 * @author emmartins
 */
public class MigrateHttpListener implements WildFly10SubsystemMigrationTaskFactory {

    private static final String SERVER = "server";
    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTP_LISTENER_NAME = "http";
    private static final String REDIRECT_SOCKET_ATTR_NAME = "redirect-socket";
    private static final String REDIRECT_SOCKET_ATTR_VALUE = "https";

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("migrate-undertow-http-listener").build();

    public static final MigrateHttpListener INSTANCE = new MigrateHttpListener();

    private MigrateHttpListener() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, SubsystemsManagement subsystemsManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemsManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, SubsystemsManagement subsystemsManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME)) {
                    final PathAddress pathAddress = subsystemsManagement.getResourcePathAddress(subsystem.getName()).append(PathElement.pathElement(SERVER, SERVER_NAME), PathElement.pathElement(HTTP_LISTENER, HTTP_LISTENER_NAME));
                    final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    op.get(NAME).set(REDIRECT_SOCKET_ATTR_NAME);
                    op.get(VALUE).set(REDIRECT_SOCKET_ATTR_VALUE);
                    subsystemsManagement.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().infof("Undertow's default HTTP listener 'redirect-socket' set to 'https'.");
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
