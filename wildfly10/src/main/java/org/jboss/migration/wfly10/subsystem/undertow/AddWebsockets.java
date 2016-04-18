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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds support for websockets.
 * @author emmartins
 */
public class AddWebsockets implements WildFly10SubsystemMigrationTask {

    private static final String SERVLET_CONTAINER = "servlet-container";
    private static final String SERVLET_CONTAINER_NAME = "default";
    private static final String SETTING = "setting";
    private static final String SETTING_NAME = "websockets";

    public static final AddWebsockets INSTANCE = new AddWebsockets();

    private AddWebsockets() {
    }
    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null) {
            return;
        }
        if (!config.hasDefined(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME, SETTING, SETTING_NAME)) {
            final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), PathElement.pathElement(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME), PathElement.pathElement(SETTING, SETTING_NAME));
            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
            server.executeManagementOperation(addOp);
            ServerMigrationLogger.ROOT_LOGGER.infof("Undertow's default Servlet Container configured to support Websockets.");
        }
    }
}
