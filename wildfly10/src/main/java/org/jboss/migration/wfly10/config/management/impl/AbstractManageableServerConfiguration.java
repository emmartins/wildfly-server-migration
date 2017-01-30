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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractManageableServerConfiguration extends AbstractManageableResource implements ManageableServerConfiguration {

    private final WildFlyServer10 server;
    private ModelControllerClient modelControllerClient;
    private final ExtensionConfigurationImpl.Factory extensionConfigurations;
    private final InterfaceResourceImpl.Factory interfaceResources;
    private final SocketBindingGroupResourceImpl.Factory socketBindingGroupResources;
    private final SystemPropertyConfigurationImpl.Factory systemPropertyResources;

    protected AbstractManageableServerConfiguration(String resourceName, PathAddress pathAddress, WildFlyServer10 server) {
        super(resourceName, pathAddress, null);
        this.server = server;
        extensionConfigurations = new ExtensionConfigurationImpl.Factory(pathAddress, this);
        interfaceResources = new InterfaceResourceImpl.Factory(pathAddress, this);
        socketBindingGroupResources = new SocketBindingGroupResourceImpl.Factory(pathAddress, this);
        systemPropertyResources = new SystemPropertyConfigurationImpl.Factory(pathAddress, this);
        addChildResourceFactory(extensionConfigurations);
        addChildResourceFactory(interfaceResources);
        addChildResourceFactory(socketBindingGroupResources);
        addChildResourceFactory(systemPropertyResources);
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
    public ModelNode executeManagementOperation(ModelNode operation) throws ManagementOperationException {
        final ModelControllerClient modelControllerClient = getModelControllerClient();
        if (modelControllerClient == null) {
            throw new IllegalStateException("configuration not started");
        }
        try {
            final ModelNode result = modelControllerClient.execute(operation);
            //ServerMigrationLogger.ROOT_LOGGER.infof("Op result %s", result.toString());
            processResult(result);
            return result;
        } catch (IOException e) {
            throw new ManagementOperationException(e);
        }
    }

    @Override
    public Path resolvePath(String pathName) throws ManagementOperationException {
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
        final PathAddress pathAddress = getSystemPropertyConfigurationPathAddress(systemPropertyName);
        executeManagementOperation(Util.createAddOperation(pathAddress));
        executeManagementOperation(Util.createRemoveOperation(pathAddress));
    }
}
