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
package org.jboss.migration.wfly10.subsystem.messaging;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemNames;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds HTTP Acceptors and Connectors to the Messaging subsystem.
 * @author emmartins
 */
public class AddHttpAcceptorsAndConnectors implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddHttpAcceptorsAndConnectors INSTANCE = new AddHttpAcceptorsAndConnectors();

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("add-messaging-http-acceptors-and-connectors").build();

    private AddHttpAcceptorsAndConnectors() {
    }

    private static final String SERVER = "server";

    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTP_LISTENER_NAME = "http";

    private static final String HTTP_ACCEPTOR = "http-acceptor";
    private static final String HTTP_ACCEPTOR_NAME = HTTP_ACCEPTOR;
    private static final String HTTP_ACCEPTOR_THROUGHPUT_NAME = "http-acceptor-throughput";

    private static final String HTTP_CONNECTOR = "http-connector";
    private static final String HTTP_CONNECTOR_NAME = HTTP_CONNECTOR;
    private static final String HTTP_CONNECTOR_THROUGHPUT_NAME = "http-connector-throughput";
    private static final String SOCKET_BINDING = "socket-binding";
    private static final String SOCKET_BINDING_NAME = "http";
    private static final String ENDPOINT = "endpoint";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // ensure undertow's default http listener is configured
                final ModelNode undertowConfig = server.getSubsystem(WildFly10SubsystemNames.UNDERTOW);
                if (undertowConfig == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                } else {
                    if (!undertowConfig.hasDefined(SERVER, SERVER_NAME, HTTP_LISTENER, HTTP_LISTENER_NAME)) {
                        context.getLogger().debug("Skipping configuration of Messaging ActiveMQ http acceptors and connectors, Undertow's default HTTP listener not found.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                }
                // add http acceptors and connectors to each messaging server
                if (config.hasDefined(SERVER)) {
                    boolean configUpdated = false;
                    for (String serverName : config.get(SERVER).keys()) {
                        if (!config.hasDefined(SERVER, serverName, HTTP_ACCEPTOR, HTTP_ACCEPTOR_NAME)) {
                            final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(SERVER, serverName), pathElement(HTTP_ACCEPTOR, HTTP_ACCEPTOR_NAME));
                            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                            addOp.get(HTTP_LISTENER).set(HTTP_LISTENER_NAME);
                            server.executeManagementOperation(addOp);
                            configUpdated = true;
                            context.getLogger().infof("HTTP Acceptor named %s added to Messaging ActiveMQ subsystem configuration.", HTTP_ACCEPTOR_NAME);
                        }
                /*
                if (!config.hasDefined(SERVER, serverName, HTTP_ACCEPTOR, HTTP_ACCEPTOR_THROUGHPUT_NAME)) {
                    final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(SERVER, serverName), pathElement(HTTP_ACCEPTOR, HTTP_ACCEPTOR_THROUGHPUT_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    addOp.get(HTTP_LISTENER).set(HTTP_LISTENER_NAME);
                    server.executeManagementOperation(addOp);
                    context.getLogger().infof("HTTP Acceptor named %s added to Messaging ActiveMQ subsystem configuration.", HTTP_ACCEPTOR_THROUGHPUT_NAME);
                }
                */
                        if (!config.hasDefined(SERVER, serverName, HTTP_CONNECTOR, HTTP_CONNECTOR_NAME)) {
                            final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(SERVER, serverName), pathElement(HTTP_CONNECTOR, HTTP_CONNECTOR_NAME));
                            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                            addOp.get(SOCKET_BINDING).set(SOCKET_BINDING_NAME);
                            addOp.get(ENDPOINT).set(HTTP_ACCEPTOR_NAME);
                            server.executeManagementOperation(addOp);
                            configUpdated = true;
                            context.getLogger().infof("HTTP Connector named %s added to Messaging ActiveMQ subsystem configuration.", HTTP_CONNECTOR_NAME);
                        }
                /*
                if (!config.hasDefined(SERVER, serverName, HTTP_CONNECTOR, HTTP_CONNECTOR_THROUGHPUT_NAME)) {
                    final PathAddress pathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(SERVER, serverName), pathElement(HTTP_CONNECTOR, HTTP_CONNECTOR_THROUGHPUT_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    addOp.get(SOCKET_BINDING).set(SOCKET_BINDING_NAME);
                    addOp.get(ENDPOINT).set(HTTP_ACCEPTOR_NAME);
                    server.executeManagementOperation(addOp);
                    context.getLogger().infof("HTTP Connector named %s added to Messaging ActiveMQ subsystem configuration.", HTTP_CONNECTOR_THROUGHPUT_NAME);
                }
                */
                    }
                    return configUpdated ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
                } else {
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
