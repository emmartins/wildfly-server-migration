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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.task.SocketBindingsMigration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * Updates the management-https socket binding config.
 * @author emmartins
 */
public class UpdateManagementHttpsSocketBinding implements SocketBindingsMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "update-management-https";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of the management-https socket binding related properties
         */
        String PROPERTIES_PREFIX = SocketBindingsMigration.SOCKET_BINDINGS + "." + SERVER_MIGRATION_TASK_NAME_NAME + ".";

        String PORT = PROPERTIES_PREFIX + "port";
    }

    public static final String DEFAULT_PORT = "${jboss.management.https.port:9993}";

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, final SocketBindingsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTask subtask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final MigrationEnvironment env = context.getServerMigrationContext().getMigrationEnvironment();
                String envPropertyPort = env.getPropertyAsString(EnvironmentProperties.PORT);
                if (envPropertyPort == null || envPropertyPort.isEmpty()) {
                    envPropertyPort = DEFAULT_PORT;
                }
                if (!resourceManagement.getResourceNames().contains("management-https")) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // management-https binding found, update port
                final PathAddress pathAddress = resourceManagement.getResourcePathAddress("management-https");
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set("port");
                writeAttrOp.get(VALUE).set(envPropertyPort);
                resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().infof("Socket binding 'management-https' default port set to "+envPropertyPort+".");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        subtasks.add(subtask);
    }
}