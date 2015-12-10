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

import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.EmbeddedWildFly10StandaloneServer;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;

/**
 * @author emmartins
 */
public class EJb3WildFly10Extension extends WildFly10Extension {

    public static final EJb3WildFly10Extension INSTANCE = new EJb3WildFly10Extension();

    private EJb3WildFly10Extension() {
        super("org.jboss.as.ejb3");
        subsystems.add(Ejb3WildFly10Subsystem.INSTANCE);
    }

    public static class Ejb3WildFly10Subsystem extends BasicWildFly10Subsystem {
        public static final Ejb3WildFly10Subsystem INSTANCE = new Ejb3WildFly10Subsystem();
        private Ejb3WildFly10Subsystem() {
            super("ejb3", EJb3WildFly10Extension.INSTANCE);
        }

        @Override
        public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            super.migrate(server, context);
            final ModelNode config = server.getSubsystem(getName());
            processConfig(config, server, context);
            // TODO fix clustering attr
        }

        protected void processConfig(ModelNode config, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
            ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s config after migration: %s", getName(), config.asString());
            ((EmbeddedWildFly10StandaloneServer)server).wfly5520Workaround();
        }
    }
}