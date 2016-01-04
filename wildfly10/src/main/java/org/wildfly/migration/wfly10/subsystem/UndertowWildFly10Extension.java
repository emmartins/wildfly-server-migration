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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
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
public class UndertowWildFly10Extension extends WildFly10Extension {

    private static final String EXTENSION_NAME = "org.wildfly.extension.undertow";
    public static final UndertowWildFly10Extension INSTANCE = new UndertowWildFly10Extension();

    private UndertowWildFly10Extension() {
        super(EXTENSION_NAME);
        subsystems.add(new UndertowWildFly10Subsystem(this));
    }

    private static class UndertowWildFly10Subsystem extends BasicWildFly10Subsystem {

        private static final String SUBSYSTEM_NAME = "undertow";
        private static final String BUFFER_CACHE = "buffer-cache";
        private static final String BUFFER_CACHE_NAME = "default";
        private static final String SERVER = "server";
        private static final String SERVER_NAME = "default-server";
        private static final String HTTP_LISTENER = "http-listener";
        private static final String HTTP_LISTENER_NAME = "http";
        private static final String REDIRECT_SOCKET_ATTR_NAME = "redirect-socket";
        private static final String REDIRECT_SOCKET_ATTR_VALUE = "https";
        private static final String SERVLET_CONTAINER = "servlet-container";
        private static final String SERVLET_CONTAINER_NAME = "default";
        private static final String SETTING = "setting";
        private static final String SETTING_NAME = "websockets";

        private UndertowWildFly10Subsystem(UndertowWildFly10Extension extension) {
            super(SUBSYSTEM_NAME, extension);
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
            addBufferCache(config, server);
            migrateHttpListener(config, server);
            addWebsockets(config, server);
            // TODO add header filters?
        }

        private void migrateHttpListener(ModelNode config, WildFly10StandaloneServer server) throws IOException {
            if (config.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME)) {
                final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, getName()), PathElement.pathElement(SERVER, SERVER_NAME), PathElement.pathElement(HTTP_LISTENER, HTTP_LISTENER_NAME));
                final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                op.get(NAME).set(REDIRECT_SOCKET_ATTR_NAME);
                op.get(VALUE).set(REDIRECT_SOCKET_ATTR_VALUE);
                server.executeManagementOperation(op);
                ServerMigrationLogger.ROOT_LOGGER.infof("Undertow's default HTTP listener 'redirect-socket' set to 'https'.");
            }
        }

        private void addWebsockets(ModelNode config, WildFly10StandaloneServer server) throws IOException {
            if (!config.hasDefined(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME, SETTING, SETTING_NAME)) {
                final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, getName()), PathElement.pathElement(SERVLET_CONTAINER, SERVLET_CONTAINER_NAME), PathElement.pathElement(SETTING, SETTING_NAME));
                final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                server.executeManagementOperation(addOp);
                ServerMigrationLogger.ROOT_LOGGER.infof("Undertow's default Servlet Container configured to support Websockets.");
            }
        }

        private void addBufferCache(ModelNode config, WildFly10StandaloneServer server) throws IOException {
            if (!config.hasDefined(BUFFER_CACHE, BUFFER_CACHE_NAME)) {
                final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, getName()), PathElement.pathElement(BUFFER_CACHE, BUFFER_CACHE_NAME));
                final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                server.executeManagementOperation(addOp);
                ServerMigrationLogger.ROOT_LOGGER.infof("Undertow's default buffer cache added.");
            }
        }
    }
}