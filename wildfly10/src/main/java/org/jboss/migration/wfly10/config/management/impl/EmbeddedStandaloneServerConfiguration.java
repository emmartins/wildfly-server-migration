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
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.management.DeploymentsManagement;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

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
    private final ExtensionsManagement extensionsManagement;
    private final InterfacesManagement interfacesManagement;
    private final ManagementInterfacesManagement managementInterfacesManagement;
    private final SecurityRealmsManagement securityRealmsManagement;
    private final SocketBindingGroupsManagement socketBindingGroupsManagement;
    private final SubsystemsManagement subsystemsManagement;

    public EmbeddedStandaloneServerConfiguration(String config, WildFly10Server server) {
        super(server);
        this.config = config;
        this.deploymentsManagement = new DeploymentsManagementImpl(null, this);
        this.extensionsManagement = new ExtensionsManagementImpl(null, this) {
            @Override
            public Set<String> getSubsystems() throws IOException {
                return getSubsystemsManagement().getResourceNames();
            }
        };
        this.subsystemsManagement = new SubsystemsManagementImpl(null, this);
        this.interfacesManagement = new InterfacesManagementImpl(null, this);
        this.socketBindingGroupsManagement = new SocketBindingGroupsManagementImpl(null, this);
        final PathAddress managementCoreServicePathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmsManagement = new SecurityRealmsManagementImpl(managementCoreServicePathAddress, this);
        this.managementInterfacesManagement = new ManagementInterfacesManagementImpl(managementCoreServicePathAddress, this);
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        final String[] cmds = {"--server-config="+config,"--admin-only"};
        standaloneServer = EmbeddedProcessFactory.createStandaloneServer(getServer().getBaseDir().toString(), null, null, cmds);
        try {
            standaloneServer.start();
        } catch (EmbeddedProcessStartException e) {
            throw new RuntimeException(e);
        }
        return standaloneServer.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        standaloneServer.stop();
        standaloneServer = null;
    }

    public DeploymentsManagement getDeploymentsManagement() {
        return deploymentsManagement;
    }

    @Override
    public ExtensionsManagement getExtensionsManagement() {
        return extensionsManagement;
    }

    @Override
    public InterfacesManagement getInterfacesManagement() {
        return interfacesManagement;
    }

    @Override
    public SecurityRealmsManagement getSecurityRealmsManagement() {
        return securityRealmsManagement;
    }

    @Override
    public SocketBindingGroupsManagement getSocketBindingGroupsManagement() {
        return socketBindingGroupsManagement;
    }

    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }

    @Override
    public ManagementInterfacesManagement getManagementInterfacesManagement() {
        return managementInterfacesManagement;
    }

    public static class ConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public StandaloneServerConfiguration getManageableConfiguration(Path configFile, WildFly10Server server) {
            return new EmbeddedStandaloneServerConfiguration(configFile.getFileName().toString(), server);
        }
    }
}
