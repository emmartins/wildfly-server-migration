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

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;
import org.jboss.migration.wfly10.config.management.ManageableResource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractManageableServerConfiguration implements ManageableServerConfiguration {

    private final WildFlyServer10 server;
    private ModelControllerClient modelControllerClient;

    protected AbstractManageableServerConfiguration(WildFlyServer10 server) {
        this.server = server;
    }

    @Override
    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("server started");
        }
        modelControllerClient = startConfiguration();
    }

    protected abstract ModelControllerClient startConfiguration();

    @Override
    public void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("server not started");
        }
        stopConfiguration();
        modelControllerClient = null;
    }

    protected abstract void stopConfiguration();

    @Override
    public boolean isStarted() {
        return modelControllerClient != null;
    }

    @Override
    public WildFlyServer10 getServer() {
        return server;
    }

    protected void processResult(ModelNode result) throws ManagementOperationException {
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new ManagementOperationException(result.get(FAILURE_DESCRIPTION).asString());
        }
    }

    @Override
    public ModelNode executeManagementOperation(ModelNode operation) throws IOException {
        final ModelControllerClient modelControllerClient = getModelControllerClient();
        if (modelControllerClient == null) {
            throw new IllegalStateException("configuration not started");
        }
        final ModelNode result = modelControllerClient.execute(operation);
        //ServerMigrationLogger.ROOT_LOGGER.infof("Op result %s", result.toString());
        processResult(result);
        return  result;
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

    @Override
    public ModelControllerClient getModelControllerClient() {
        return modelControllerClient;
    }

    protected void writeConfiguration() {
        // force write of xml config by tmp setting a system property
        final String systemPropertyName = "org.jboss.migration.tmp."+System.nanoTime();
        final PathAddress pathAddress = getSystemPropertiesManagement().getResourcePathAddress(systemPropertyName);
        try {
            executeManagementOperation(Util.createAddOperation(pathAddress));
            executeManagementOperation(Util.createRemoveOperation(pathAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ManageableResource> getResources() {
        final List<ManageableResource> result = new ArrayList<>();
        result.add(getExtensionsManagement());
        result.addAll(getExtensionsManagement().getChildren());

        if (type == ExtensionsManagement.class)
            return null;

    }

    @Override
    public <T extends ManageableResource> List<T> getResources(Class<T> type) {
        final List<T> result = new ArrayList<>();
        if (type.isAssignableFrom(ExtensionsManagement.class)) {
            result.add((T) getExtensionsManagement());
        } else
        return null;
        /*
        .subtask(SubsystemsManagement.class, )
         */

    }


}
