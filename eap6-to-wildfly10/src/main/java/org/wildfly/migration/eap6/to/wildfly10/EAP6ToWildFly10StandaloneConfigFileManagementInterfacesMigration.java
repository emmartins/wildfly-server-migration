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
package org.wildfly.migration.eap6.to.wildfly10;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of management interfaces, turning on http upgrade.
 *  @author emmartins
 */
public class EAP6ToWildFly10StandaloneConfigFileManagementInterfacesMigration {

    public void run(WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating management interfaces...");
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, pathAddress(pathElement(CORE_SERVICE, MANAGEMENT)));
            op.get(CHILD_TYPE).set(MANAGEMENT_INTERFACE);
            final ModelNode opResult = target.executeManagementOperation(op);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Get management interfaces Op result %s", opResult.toString());
            for (ModelNode resultItem : opResult.get(RESULT).asList()) {
                if (resultItem.asString().equals("http-interface")) {
                    // http interface found, turn on http upgrade
                    final PathAddress pathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT), pathElement(MANAGEMENT_INTERFACE, "http-interface"));
                    final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    writeAttrOp.get(NAME).set("http-upgrade-enabled");
                    writeAttrOp.get(VALUE).set(true);
                    target.executeManagementOperation(writeAttrOp);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Activated HTTP Management Interface's support for HTTP Upgrade.");
                }
            }
        } finally {
            if (!targetStarted) {
                target.stop();
            }
            ServerMigrationLogger.ROOT_LOGGER.info("Management interfaces migration done.");
        }
    }
}