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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of management interfaces, turning on http upgrade.
 *  @author emmartins
 */
public class EAP6ToEAP7StandaloneConfigFileManagementInterfacesMigration {

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("Management Interfaces Migration").build();

    public ServerMigrationTask getServerMigrationTask(final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                final boolean targetStarted = target.isStarted();
                if (!targetStarted) {
                    target.start();
                }
                try {
                    final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, pathAddress(pathElement(CORE_SERVICE, MANAGEMENT)));
                    op.get(CHILD_TYPE).set(MANAGEMENT_INTERFACE);
                    final ModelNode opResult = target.executeManagementOperation(op);
                    for (ModelNode resultItem : opResult.get(RESULT).asList()) {
                        if (resultItem.asString().equals("http-interface")) {
                            // http interface found, turn on http upgrade
                            final PathAddress pathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT), pathElement(MANAGEMENT_INTERFACE, "http-interface"));
                            final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                            writeAttrOp.get(NAME).set("http-upgrade-enabled");
                            writeAttrOp.get(VALUE).set(true);
                            target.executeManagementOperation(writeAttrOp);
                            ServerMigrationLogger.ROOT_LOGGER.infof("Activated HTTP Management Interface's support for HTTP Upgrade.");
                            // TODO use a subtask per http interface migrated
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    }
                } finally {
                    if (!targetStarted) {
                        target.stop();
                    }
                }
                return ServerMigrationTaskResult.SKIPPED;
            }
        };
    }
}