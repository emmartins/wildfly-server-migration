/*
 * Copyright 2015 Red Hat, Inc.
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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class BeanValidationWildFly10Extension extends WildFly10Extension {

    public static final BeanValidationWildFly10Extension INSTANCE = new BeanValidationWildFly10Extension();

    private BeanValidationWildFly10Extension() {
        super("org.wildfly.extension.bean-validation");
        subsystems.add(new BeanValidationWildFly10Subsystem(this));
    }

    private static class BeanValidationWildFly10Subsystem extends BasicWildFly10Subsystem {
        private BeanValidationWildFly10Subsystem(BeanValidationWildFly10Extension extension) {
            super("bean-validation", extension);
        }

        @Override
        public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            super.migrate(server, context);
            final ModelNode config = server.getSubsystems().contains(getName()) ? server.getSubsystem(getName()) : null;
            migrateConfig(config, server, context);
        }

        protected void migrateConfig(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            if (config != null) {
                return;
            }
            final String extensionName = getExtension().getName();
            if (!server.getExtensions().contains(extensionName)) {
                ServerMigrationLogger.ROOT_LOGGER.debugf("Adding Extension %s...", extensionName);
                final ModelNode op = Util.createAddOperation(pathAddress(pathElement(EXTENSION, extensionName)));
                op.get(MODULE).set(extensionName);
                server.executeManagementOperation(op);
                ServerMigrationLogger.ROOT_LOGGER.infof("Extension %s added.",extensionName);
            }
            ServerMigrationLogger.ROOT_LOGGER.debugf("Adding subsystem %s...", getName());
            final ModelNode op = Util.createAddOperation(pathAddress(pathElement(SUBSYSTEM, getName())));
            server.executeManagementOperation(op);
            ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s added.", getName());
        }
    }
}