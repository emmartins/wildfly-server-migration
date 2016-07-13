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
package org.jboss.migration.eap6.to.eap7;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Updates the management-https socket binding config.
 * @author emmartins
 */
public class UpdateManagementHttpsSocketBinding implements ServerMigrationTask {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "update-management-https";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of the management-https socket binding related properties
         */
        String PROPERTIES_PREFIX = EAP6ToEAP7StandaloneConfigFileSocketBindingsMigration.EnvironmentProperties.PROPERTIES_PREFIX + SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips the task execution
         */
        String SKIP = PROPERTIES_PREFIX + "skip";

        String PORT = PROPERTIES_PREFIX + "port";
    }

    public static final String DEFAULT_PORT = "${jboss.management.https.port:9993}";

    private final WildFly10StandaloneServer target;


    public UpdateManagementHttpsSocketBinding(WildFly10StandaloneServer target) {
        this.target = target;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        final MigrationEnvironment env = context.getServerMigrationContext().getMigrationEnvironment();
        if (!env.getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
            String envPropertyPort = env.getPropertyAsString(EnvironmentProperties.PORT);
            if (envPropertyPort == null || envPropertyPort.isEmpty()) {
                envPropertyPort = DEFAULT_PORT;
            }
            final boolean targetStarted = target.isStarted();
            if (!targetStarted) {
                target.start();
            }
            try {
                final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_RESOURCES_OPERATION, pathAddress(pathElement(SOCKET_BINDING_GROUP, "standard-sockets")));
                op.get(CHILD_TYPE).set(SOCKET_BINDING);
                op.get(RECURSIVE).set(true);
                final ModelNode opResult = target.executeManagementOperation(op);
                context.getLogger().debugf("Get socket bindings Op result %s", opResult.toString());
                for (ModelNode resultItem : opResult.get(RESULT).asList()) {
                    final Property socketBinding = resultItem.asProperty();
                    if (socketBinding.getName().equals("management-https")) {
                        // management-https binding found, update port
                        final PathAddress pathAddress = pathAddress(pathElement(SOCKET_BINDING_GROUP, "standard-sockets"), pathElement(SOCKET_BINDING, "management-https"));
                        final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                        writeAttrOp.get(NAME).set("port");
                        writeAttrOp.get(VALUE).set(envPropertyPort);
                        target.executeManagementOperation(writeAttrOp);
                        context.getLogger().infof("Socket binding 'management-https' default port set to "+envPropertyPort+".");
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                }
            } finally {
                if (!targetStarted) {
                    target.stop();
                }
            }
        }
        return ServerMigrationTaskResult.SKIPPED;
    }
}