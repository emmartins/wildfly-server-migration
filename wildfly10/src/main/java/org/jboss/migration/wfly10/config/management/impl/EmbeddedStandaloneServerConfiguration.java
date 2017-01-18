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
import org.jboss.migration.wfly10.config.management.DeploymentsManagement;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;

/**
 * @author emmartins
 */
public class EmbeddedStandaloneServerConfiguration extends AbstractManageableServerConfiguration implements StandaloneServerConfiguration {

    private final String config;
    private StandaloneServer standaloneServer;
    private final DeploymentsManagement deploymentsManagement;
    private final ManagementInterfacesManagement managementInterfacesManagement;
    private final SecurityRealmsManagement securityRealmsManagement;
    private final SubsystemsManagement subsystemsManagement;

    public EmbeddedStandaloneServerConfiguration(String config, WildFlyServer10 server) {
        super(server, PathAddress.EMPTY_ADDRESS);
        this.config = config;
        this.deploymentsManagement = new DeploymentsManagementImpl(pathAddress, this);
        this.subsystemsManagement = new SubsystemsManagementImpl(pathAddress, this);
        final PathAddress managementCoreServicePathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmsManagement = new SecurityRealmsManagementImpl(managementCoreServicePathAddress, this);
        this.managementInterfacesManagement = new ManagementInterfacesManagementImpl(managementCoreServicePathAddress, this);
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

    public DeploymentsManagement getDeploymentsManagement() {
        return deploymentsManagement;
    }

    @Override
    public ManagementInterfacesManagement getManagementInterfacesManagement() {
        return managementInterfacesManagement;
    }

    @Override
    public SecurityRealmsManagement getSecurityRealmsManagement() {
        return securityRealmsManagement;
    }

    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }


    public static class ConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public StandaloneServerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedStandaloneServerConfiguration(configFile.getFileName().toString(), server);
        }
    }

    @Override
    public <T extends ManageableResources> List<T> findResources(Class<T> resourcesType) throws IOException {
        final List<T> result = super.findResources(resourcesType);
        findResources(getDeploymentsManagement(), resourcesType, result);
        findResources(getManagementInterfacesManagement(), resourcesType, result);
        findResources(getSecurityRealmsManagement(), resourcesType, result);
        findResources(getSubsystemsManagement(), resourcesType, result);
        return result;
    }
}
