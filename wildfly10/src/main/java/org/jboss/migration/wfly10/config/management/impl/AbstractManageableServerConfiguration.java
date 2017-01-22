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
import org.jboss.migration.wfly10.config.management.ExtensionConfiguration;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResource;
import org.jboss.migration.wfly10.config.management.SystemPropertyConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

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
        final PathAddress pathAddress = getSystemPropertyConfigurationPathAddress(systemPropertyName);
        try {
            executeManagementOperation(Util.createAddOperation(pathAddress));
            executeManagementOperation(Util.createRemoveOperation(pathAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExtensionConfiguration getExtensionConfiguration(String resourceName) throws IOException {
        return extensionConfigurations.getResource(resourceName);
    }

    @Override
    public List<ExtensionConfiguration> getExtensionConfigurations() throws IOException {
        return extensionConfigurations.getResources();
    }

    @Override
    public Set<String> getExtensionConfigurationNames() throws IOException {
        return extensionConfigurations.getResourceNames();
    }

    @Override
    public PathAddress getExtensionConfigurationPathAddress(String resourceName) {
        return extensionConfigurations.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeExtensionConfiguration(String resourceName) throws IOException {
        extensionConfigurations.removeResource(resourceName);
    }

    @Override
    public InterfaceResource getInterfaceResource(String resourceName) throws IOException {
        return interfaceResources.getResource(resourceName);
    }

    @Override
    public List<InterfaceResource> getInterfaceResources() throws IOException {
        return interfaceResources.getResources();
    }

    @Override
    public Set<String> getInterfaceResourceNames() throws IOException {
        return interfaceResources.getResourceNames();
    }

    @Override
    public PathAddress getInterfaceResourcePathAddress(String resourceName) {
        return interfaceResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeInterfaceResource(String resourceName) throws IOException {
        interfaceResources.removeResource(resourceName);
    }

    @Override
    public SocketBindingGroupResource getSocketBindingGroupResource(String resourceName) throws IOException {
        return socketBindingGroupResources.getResource(resourceName);
    }

    @Override
    public List<SocketBindingGroupResource> getSocketBindingGroupResources() throws IOException {
        return socketBindingGroupResources.getResources();
    }

    @Override
    public Set<String> getSocketBindingGroupResourceNames() throws IOException {
        return socketBindingGroupResources.getResourceNames();
    }

    @Override
    public PathAddress getSocketBindingGroupResourcePathAddress(String resourceName) {
        return socketBindingGroupResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeSocketBindingGroupResource(String resourceName) throws IOException {
        socketBindingGroupResources.removeResource(resourceName);
    }

    @Override
    public SystemPropertyConfiguration getSystemPropertyConfiguration(String resourceName) throws IOException {
        return systemPropertyResources.getResource(resourceName);
    }

    @Override
    public List<SystemPropertyConfiguration> getSystemPropertyConfigurations() throws IOException {
        return systemPropertyResources.getResources();
    }

    @Override
    public Set<String> getSystemPropertyConfigurationNames() throws IOException {
        return systemPropertyResources.getResourceNames();
    }

    @Override
    public PathAddress getSystemPropertyConfigurationPathAddress(String resourceName) {
        return systemPropertyResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeSystemPropertyConfiguration(String resourceName) throws IOException {
        systemPropertyResources.removeResource(resourceName);
    }
}
