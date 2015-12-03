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
package org.wildfly.migration.wildfly.server.embedded;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.core.embedded.StandaloneServer;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wildfly.server.TargetServerManagement;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
class EmbeddedTargetServerManagement implements TargetServerManagement {

    private final StandaloneServer standaloneServer;

    EmbeddedTargetServerManagement(StandaloneServer standaloneServer) {
        this.standaloneServer = standaloneServer;
    }

    @Override
    public Set<String> getExtensions() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(EXTENSION);
        final ModelNode opResult = standaloneServer.getModelControllerClient().execute(op);
        processResult(opResult);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get Extensions Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public Set<String> getSubsystems() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(SUBSYSTEM);
        final ModelNode opResult = standaloneServer.getModelControllerClient().execute(op);
        processResult(opResult);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get subsystems Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public void removeSubsystem(String subsystem) throws IOException {
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem));
        final ModelNode op = Util.createRemoveOperation(address);
        processResult(standaloneServer.getModelControllerClient().execute(op));
    }

    @Override
    public void removeExtension(String extension) throws IOException {
        final PathAddress address = pathAddress(pathElement(EXTENSION, extension));
        final ModelNode op = Util.createRemoveOperation(address);
        processResult(standaloneServer.getModelControllerClient().execute(op));
    }

    @Override
    public void migrateSubsystem(String subsystem) throws IOException {
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem));
        final ModelNode op = Util.createEmptyOperation("migrate", address);
        final ModelNode result = standaloneServer.getModelControllerClient().execute(op);
        //processResult(result);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Op result %s", result.toString());
    }

    private void processResult(ModelNode result) {
        if(!"success".equals(result.get("outcome").asString())) {
            throw new RuntimeException(result.get("failure-description").asString());
        }
    }
}
