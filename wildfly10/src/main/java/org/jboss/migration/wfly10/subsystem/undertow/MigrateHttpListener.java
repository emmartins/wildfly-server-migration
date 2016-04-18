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
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

/**
 * A task which updates the config of the default http listener.
 * @author emmartins
 */
public class MigrateHttpListener implements WildFly10SubsystemMigrationTask {

    private static final String SERVER = "server";
    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTP_LISTENER_NAME = "http";
    private static final String REDIRECT_SOCKET_ATTR_NAME = "redirect-socket";
    private static final String REDIRECT_SOCKET_ATTR_VALUE = "https";

    public static final MigrateHttpListener INSTANCE = new MigrateHttpListener();

    private MigrateHttpListener() {
    }

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null) {
            return;
        }
        if (config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME)) {
            final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), PathElement.pathElement(SERVER, SERVER_NAME), PathElement.pathElement(HTTP_LISTENER, HTTP_LISTENER_NAME));
            final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
            op.get(NAME).set(REDIRECT_SOCKET_ATTR_NAME);
            op.get(VALUE).set(REDIRECT_SOCKET_ATTR_VALUE);
            server.executeManagementOperation(op);
            ServerMigrationLogger.ROOT_LOGGER.infof("Undertow's default HTTP listener 'redirect-socket' set to 'https'.");
        }
    }
}
