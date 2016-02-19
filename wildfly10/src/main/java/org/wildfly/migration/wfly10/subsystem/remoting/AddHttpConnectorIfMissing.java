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
package org.wildfly.migration.wfly10.subsystem.remoting;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.wildfly.migration.wfly10.subsystem.WildFly10Subsystem;
import org.wildfly.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds the default http connector, if missing from the remote subsystem config.
 * @author emmartins
 */
public class AddHttpConnectorIfMissing implements WildFly10SubsystemMigrationTask {

    public static final AddHttpConnectorIfMissing INSTANCE = new AddHttpConnectorIfMissing();

    private AddHttpConnectorIfMissing() {
    }

    private static final String HTTP_CONNECTOR = "http-connector";
    private static final String HTTP_CONNECTOR_NAME = "http-remoting-connector";
    private static final String CONNECTOR_REF_ATTR_NAME = "connector-ref";
    private static final String CONNECTOR_REF_ATTR_VALUE = "http";
    private static final String SECURITY_REALM_ATTR_NAME = "security-realm";
    private static final String SECURITY_REALM_ATTR_VALUE = "ApplicationRealm";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null) {
            return;
        }
        final PathElement subsystemPathElement = pathElement(SUBSYSTEM, subsystem.getName());
        // if not defined add http connector
        if (!config.hasDefined(HTTP_CONNECTOR, HTTP_CONNECTOR_NAME)) {
            final PathAddress httpRemotingConnectorPathAddress = pathAddress(subsystemPathElement, PathElement.pathElement(HTTP_CONNECTOR, HTTP_CONNECTOR_NAME));
            final ModelNode httpRemotingConnectorAddOp = Util.createEmptyOperation(ADD, httpRemotingConnectorPathAddress);
            httpRemotingConnectorAddOp.get(CONNECTOR_REF_ATTR_NAME).set(CONNECTOR_REF_ATTR_VALUE);
            httpRemotingConnectorAddOp.get(SECURITY_REALM_ATTR_NAME).set(SECURITY_REALM_ATTR_VALUE);
            server.executeManagementOperation(httpRemotingConnectorAddOp);
            ServerMigrationLogger.ROOT_LOGGER.infof("Http connector %s added to Remoting subsystem configuration.", HTTP_CONNECTOR_NAME);
        }
    }
}
