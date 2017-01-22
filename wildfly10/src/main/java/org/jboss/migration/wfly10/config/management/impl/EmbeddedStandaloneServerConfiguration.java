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
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;

/**
 * @author emmartins
 */
public class EmbeddedStandaloneServerConfiguration extends AbstractManageableServerConfiguration implements StandaloneServerConfiguration {

    private final String config;
    private StandaloneServer standaloneServer;

    private final DeploymentResourceImpl.Factory deploymentResources;
    private final ManagementInterfaceResourceImpl.Factory managementInterfaceResources;
    private final SecurityRealmResourceImpl.Factory securityRealmResources;
    private final SubsystemConfigurationImpl.Factory subsystemResources;

    public EmbeddedStandaloneServerConfiguration(String config, WildFlyServer10 server) {
        super("", PathAddress.EMPTY_ADDRESS, server);
        this.config = config;
        deploymentResources = new DeploymentResourceImpl.Factory(getResourcePathAddress(), this);
        addChildResourceFactory(deploymentResources);
        subsystemResources = new SubsystemConfigurationImpl.Factory(getResourcePathAddress(), this);
        addChildResourceFactory(subsystemResources);
        final PathAddress managementCoreServicePathAddress = getResourcePathAddress().append(CORE_SERVICE, MANAGEMENT);
        managementInterfaceResources = new ManagementInterfaceResourceImpl.Factory(managementCoreServicePathAddress, this);
        addChildResourceFactory(managementInterfaceResources);
        securityRealmResources = new SecurityRealmResourceImpl.Factory(managementCoreServicePathAddress, this);
        addChildResourceFactory(securityRealmResources);
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        final String[] cmds = {"--server-config="+config,"--admin-only"};
        final String[] systemPackages = {"org.jboss.logmanager"};
        standaloneServer = EmbeddedProcessFactory.createStandaloneServer(getServer().getBaseDir().toString(), null, systemPackages, cmds);
        try {
            standaloneServer.start();
        } catch (EmbeddedProcessStartException e) {
            throw new RuntimeException(e);
        }
        return standaloneServer.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        writeConfiguration();
        standaloneServer.stop();
        standaloneServer = null;
    }

    @Override
    public DeploymentResource getDeploymentResource(String resourceName) throws IOException {
        return deploymentResources.getResource(resourceName);
    }

    @Override
    public List<DeploymentResource> getDeploymentResources() throws IOException {
        return deploymentResources.getResources();
    }

    @Override
    public Set<String> getDeploymentResourceNames() throws IOException {
        return deploymentResources.getResourceNames();
    }

    @Override
    public PathAddress getDeploymentResourcePathAddress(String resourceName) {
        return deploymentResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeDeploymentResource(String resourceName) throws IOException {
        deploymentResources.removeResource(resourceName);
    }

    @Override
    public ManagementInterfaceResource getManagementInterfaceResource(String resourceName) throws IOException {
        return managementInterfaceResources.getResource(resourceName);
    }

    @Override
    public List<ManagementInterfaceResource> getManagementInterfaceResources() throws IOException {
        return managementInterfaceResources.getResources();
    }

    @Override
    public Set<String> getManagementInterfaceResourceNames() throws IOException {
        return managementInterfaceResources.getResourceNames();
    }

    @Override
    public PathAddress getManagementInterfaceResourcePathAddress(String resourceName) {
        return managementInterfaceResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeManagementInterfaceResource(String resourceName) throws IOException {
        managementInterfaceResources.removeResource(resourceName);
    }

    @Override
    public SecurityRealmResource getSecurityRealmResource(String resourceName) throws IOException {
        return securityRealmResources.getResource(resourceName);
    }

    @Override
    public List<SecurityRealmResource> getSecurityRealmResources() throws IOException {
        return securityRealmResources.getResources();
    }

    @Override
    public Set<String> getSecurityRealmResourceNames() throws IOException {
        return securityRealmResources.getResourceNames();
    }

    @Override
    public PathAddress getSecurityRealmResourcePathAddress(String resourceName) {
        return securityRealmResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeSecurityRealmResource(String resourceName) throws IOException {
        securityRealmResources.removeResource(resourceName);
    }

    @Override
    public SubsystemConfiguration getSubsystemConfiguration(String resourceName) throws IOException {
        return subsystemResources.getResource(resourceName);
    }

    @Override
    public List<SubsystemConfiguration> getSubsystemConfigurations() throws IOException {
        return subsystemResources.getResources();
    }

    @Override
    public Set<String> getSubsystemConfigurationNames() throws IOException {
        return subsystemResources.getResourceNames();
    }

    @Override
    public PathAddress getSubsystemConfigurationPathAddress(String resourceName) {
        return subsystemResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeSubsystemConfiguration(String resourceName) throws IOException {
        subsystemResources.removeResource(resourceName);
    }

    public static class ConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public StandaloneServerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedStandaloneServerConfiguration(configFile.getFileName().toString(), server);
        }
    }


}
