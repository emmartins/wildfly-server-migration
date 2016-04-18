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
package org.jboss.migration.wfly10.standalone;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.WildFly10Server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedWildFly10StandaloneServer implements WildFly10StandaloneServer {

    private final String config;
    private StandaloneServer standaloneServer;
    private final WildFly10Server server;

    public EmbeddedWildFly10StandaloneServer(String config, WildFly10Server server) {
        this.config = config;
        this.server = server;
    }

    @Override
    public synchronized void start() {
        if (isStarted()) {
            throw new IllegalStateException("server started");
        }
        final String[] cmds = {"--server-config="+config,"--admin-only"};
        standaloneServer = EmbeddedServerFactory.create(server.getBaseDir().toString(), null, null, cmds);
        try {
            standaloneServer.start();
        } catch (ServerStartException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("server not started");
        }
        standaloneServer.stop();
        standaloneServer = null;
    }

    @Override
    public boolean isStarted() {
        return standaloneServer != null;
    }

    @Override
    public Set<String> getExtensions() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(EXTENSION);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get Extensions Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public WildFly10Server getServer() {
        return server;
    }

    @Override
    public ModelNode getSubsystem(String subsystem) throws IOException {
        if (!getSubsystems().contains(subsystem)) {
            return null;
        }
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem));
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Op result %s", result.toString());
        return result.get(RESULT);
    }

    @Override
    public Set<String> getSubsystems() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(SUBSYSTEM);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get subsystems Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public List<ModelNode> getSecurityRealms() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_RESOURCES_OPERATION, pathAddress(pathElement(CORE_SERVICE, MANAGEMENT)));
        op.get(CHILD_TYPE).set(SECURITY_REALM);
        op.get(RECURSIVE).set(true);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get security realms Op result %s", opResult.toString());
        return opResult.get(RESULT).asList();
    }

    @Override
    public void removeSubsystem(String subsystem) throws IOException {
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem));
        final ModelNode op = Util.createRemoveOperation(address);
        executeManagementOperation(op);
    }

    @Override
    public void removeExtension(String extension) throws IOException {
        final PathAddress address = pathAddress(pathElement(EXTENSION, extension));
        final ModelNode op = Util.createRemoveOperation(address);
        executeManagementOperation(op);
    }

    @Override
    public Path resolvePath(String pathName) throws IOException {
        final PathAddress address = pathAddress(pathElement(PATH, pathName));
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Resolve path Op result %s", opResult.toString());
        String path = opResult.get(RESULT).get(PATH).asString();
        if (!opResult.get(RESULT).hasDefined(RELATIVE_TO)) {
            return Paths.get(path);
        } else {
            return resolvePath(opResult.get(RESULT).get(RELATIVE_TO).asString()).resolve(path);
        }
    }

    private void processResult(ModelNode result) {
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new RuntimeException(result.get(FAILURE_DESCRIPTION).asString());
        }
    }

    @Override
    public ModelNode executeManagementOperation(ModelNode operation) throws IOException {
        final ModelNode result = standaloneServer.getModelControllerClient().execute(operation);
        //ServerMigrationLogger.ROOT_LOGGER.infof("Op result %s", result.toString());
        processResult(result);
        return  result;
    }

    @Override
    public ModelControllerClient getModelControllerClient() {
        return standaloneServer.getModelControllerClient();
    }
}
