/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.config.task.subsystem.messaging;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.UpdateSubsystemTaskFactory;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds HTTP Acceptors and Connectors to the Messaging subsystem.
 * @author emmartins
 */
public class AddHttpAcceptorsAndConnectors implements UpdateSubsystemTaskFactory.SubtaskFactory {

    public interface EnvironmentProperties {
        String HTTP_ACCEPTOR_NAME = "httpAcceptorName";
        String HTTP_CONNECTOR_NAME = "httpConnectorName";
        String SOCKET_BINDING_NAME = "socketBindingName";
        String UNDERTOW_HTTP_LISTENER_NAME = "undertowHttpListenerName";
        String UNDERTOW_SERVER_NAME = "undertowServerName";
    }

    public static final String DEFAULT_HTTP_ACCEPTOR_NAME = "http-acceptor";
    public static final String DEFAULT_HTTP_CONNECTOR_NAME = "http-connector";
    public static final String DEFAULT_SOCKET_BINDING_NAME = "http";
    public static final String DEFAULT_UNDERTOW_HTTP_LISTENER_NAME = "http";
    public static final String DEFAULT_UNDERTOW_SERVER_NAME = "default-server";

    public static final AddHttpAcceptorsAndConnectors INSTANCE = new AddHttpAcceptorsAndConnectors();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("add-messaging-http-acceptors-and-connectors").build();

    private AddHttpAcceptorsAndConnectors() {
    }

    private static final String SERVER = "server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTP_ACCEPTOR = "http-acceptor";
    private static final String HTTP_ACCEPTOR_THROUGHPUT_NAME = "http-acceptor-throughput";
    private static final String HTTP_CONNECTOR = "http-connector";
    private static final String HTTP_CONNECTOR_THROUGHPUT_NAME = "http-connector-throughput";
    private static final String SOCKET_BINDING = "socket-binding";
    private static final String ENDPOINT = "endpoint";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement) {
        return new UpdateSubsystemTaskFactory.Subtask(config, subsystem, subsystemsManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress subsystemPathAddress = subsystemsManagement.getResourcePathAddress(subsystem.getName());
                final ManageableServerConfiguration configurationManagement = subsystemsManagement.getServerConfiguration();
                // read env properties
                final String httpAcceptorName = taskEnvironment.getPropertyAsString(EnvironmentProperties.HTTP_ACCEPTOR_NAME, DEFAULT_HTTP_ACCEPTOR_NAME);
                final String httpConnectorName = taskEnvironment.getPropertyAsString(EnvironmentProperties.HTTP_CONNECTOR_NAME, DEFAULT_HTTP_CONNECTOR_NAME);
                final String socketBindingName = taskEnvironment.getPropertyAsString(EnvironmentProperties.SOCKET_BINDING_NAME, DEFAULT_SOCKET_BINDING_NAME);
                final String undertowHttpListenerName = taskEnvironment.getPropertyAsString(EnvironmentProperties.UNDERTOW_HTTP_LISTENER_NAME, DEFAULT_UNDERTOW_HTTP_LISTENER_NAME);
                final String undertowServerName = taskEnvironment.getPropertyAsString(EnvironmentProperties.UNDERTOW_SERVER_NAME, DEFAULT_UNDERTOW_SERVER_NAME);
                // ensure undertow's default http listener is configured
                final ModelNode undertowConfig = subsystemsManagement.getResourceConfiguration(SubsystemNames.UNDERTOW);
                if (undertowConfig == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                } else {
                    if (!undertowConfig.hasDefined(SERVER, undertowServerName, HTTP_LISTENER, undertowHttpListenerName)) {
                        context.getLogger().debug("Skipping configuration of Messaging ActiveMQ http acceptors and connectors, Undertow's default HTTP listener not found.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                }
                // add http acceptors and connectors to each messaging server
                if (config.hasDefined(SERVER)) {
                    boolean configUpdated = false;
                    for (String serverName : config.get(SERVER).keys()) {
                        if (!config.hasDefined(SERVER, serverName, HTTP_ACCEPTOR, httpAcceptorName)) {
                            final PathAddress pathAddress = subsystemPathAddress.append(pathElement(SERVER, serverName), pathElement(HTTP_ACCEPTOR, httpAcceptorName));
                            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                            addOp.get(HTTP_LISTENER).set(undertowHttpListenerName);
                            configurationManagement.executeManagementOperation(addOp);
                            configUpdated = true;
                            context.getLogger().infof("HTTP Acceptor named %s added to Messaging ActiveMQ subsystem configuration.", httpAcceptorName);
                        }
                /*
                if (!config.hasDefined(SERVER, serverName, HTTP_ACCEPTOR, HTTP_ACCEPTOR_THROUGHPUT_NAME)) {
                    final PathAddress pathAddress = subsystemPathAddress.append(pathElement(SERVER, serverName), pathElement(HTTP_ACCEPTOR, HTTP_ACCEPTOR_THROUGHPUT_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    addOp.get(HTTP_LISTENER).set(HTTP_LISTENER_NAME);
                    configurationManagement.executeManagementOperation(addOp);
                    context.getLogger().infof("HTTP Acceptor named %s added to Messaging ActiveMQ subsystem configuration.", HTTP_ACCEPTOR_THROUGHPUT_NAME);
                }
                */
                        if (!config.hasDefined(SERVER, serverName, HTTP_CONNECTOR, httpConnectorName)) {
                            final PathAddress pathAddress = subsystemPathAddress.append(pathElement(SERVER, serverName), pathElement(HTTP_CONNECTOR, httpConnectorName));
                            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                            addOp.get(SOCKET_BINDING).set(socketBindingName);
                            addOp.get(ENDPOINT).set(httpAcceptorName);
                            configurationManagement.executeManagementOperation(addOp);
                            configUpdated = true;
                            context.getLogger().infof("HTTP Connector named %s added to Messaging ActiveMQ subsystem configuration.", httpConnectorName);
                        }
                /*
                if (!config.hasDefined(SERVER, serverName, HTTP_CONNECTOR, HTTP_CONNECTOR_THROUGHPUT_NAME)) {
                    final PathAddress pathAddress = subsystemPathAddress.append(pathElement(SERVER, serverName), pathElement(HTTP_CONNECTOR, HTTP_CONNECTOR_THROUGHPUT_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    addOp.get(SOCKET_BINDING).set(SOCKET_BINDING_NAME);
                    addOp.get(ENDPOINT).set(HTTP_ACCEPTOR_NAME);
                    configurationManagement.executeManagementOperation(addOp);
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
