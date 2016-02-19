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
package org.wildfly.migration.wfly10.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which creates a subsystem if its missing from the server's config.
 * @author emmartins
 */
public class AddSubsystemWithoutConfig implements WildFly10SubsystemMigrationTask {
    public static final AddSubsystemWithoutConfig INSTANCE = new AddSubsystemWithoutConfig();

    private AddSubsystemWithoutConfig() {
    }

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config != null) {
            return;
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Adding subsystem %s...", subsystem.getName());
        final ModelNode op = Util.createAddOperation(pathAddress(pathElement(SUBSYSTEM, subsystem.getName())));
        server.executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s added.", subsystem.getName());
    }
}
