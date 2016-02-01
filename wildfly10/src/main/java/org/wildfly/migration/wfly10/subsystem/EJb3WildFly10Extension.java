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
public class EJb3WildFly10Extension extends WildFly10Extension {

    public static final EJb3WildFly10Extension INSTANCE = new EJb3WildFly10Extension();

    private EJb3WildFly10Extension() {
        super("org.jboss.as.ejb3");
        subsystems.add(new Ejb3WildFly10Subsystem(this));
    }

    private static class Ejb3WildFly10Subsystem extends BasicWildFly10Subsystem {
        private Ejb3WildFly10Subsystem(EJb3WildFly10Extension extension) {
            super("ejb3", extension);
        }

        @Override
        public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            super.migrate(server, context);
            migrateConfig(server.getSubsystem(getName()), server, context);
        }

        protected void migrateConfig(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            if (config == null) {
                return;
            }
            migrateRemoteConfig(config, server);
        }

        private void migrateRemoteConfig(ModelNode config, WildFly10StandaloneServer server) throws IOException {
            if (!config.hasDefined(SERVICE,"remote")) {
                return;
            }
            // /subsystem=ejb3/service=remote:write-attribute(name=connector-ref,value=http-remoting-connector)
            final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress(pathElement(SUBSYSTEM, getName()), pathElement(SERVICE,"remote")));
            op.get(NAME).set("connector-ref");
            op.get(VALUE).set("http-remoting-connector");
            server.executeManagementOperation(op);
            ServerMigrationLogger.ROOT_LOGGER.infof("EJB3 subsystem's remote service configured to use HTTP Remoting connector.");
        }
    }
}