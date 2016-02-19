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
import org.jboss.dmr.Property;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of http socket bindings to use same port as WildFly 10 defaults.
 * @author emmartins
 */
public class EAP6ToWildFly10StandaloneConfigFileSocketBindingsMigration {

    public void run(WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating socket bindings...");
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_RESOURCES_OPERATION, pathAddress(pathElement(SOCKET_BINDING_GROUP, "standard-sockets")));
            op.get(CHILD_TYPE).set(SOCKET_BINDING);
            op.get(RECURSIVE).set(true);
            final ModelNode opResult = target.executeManagementOperation(op);
            ServerMigrationLogger.ROOT_LOGGER.debugf("Get socket bindings Op result %s", opResult.toString());
            for (ModelNode resultItem : opResult.get(RESULT).asList()) {
                final Property socketBinding = resultItem.asProperty();
                if (socketBinding.getName().equals("management-https")) {
                    // http interface found, turn on http upgrade
                    final PathAddress pathAddress = pathAddress(pathElement(SOCKET_BINDING_GROUP, "standard-sockets"), pathElement(SOCKET_BINDING, "management-https"));
                    final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    writeAttrOp.get(NAME).set("port");
                    writeAttrOp.get(VALUE).set("${jboss.management.https.port:9993}");
                    target.executeManagementOperation(writeAttrOp);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Socket binding 'management-https' default port set to 9993.");
                }
            }
        } finally {
            if (!targetStarted) {
                target.stop();
            }
            ServerMigrationLogger.ROOT_LOGGER.info("Socket bindings migration done.");
        }
    }
}