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
import org.jboss.migration.wfly10.config.management.*;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final SystemPropertiesManagement systemPropertiesManagement;

    public EmbeddedStandaloneServerConfiguration(String config, WildFlyServer10 server) {
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
        this.systemPropertiesManagement = new SystemPropertiesManagementImpl(null, this);
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
    public ExtensionsManagement getExtensionsManagement() {
        return extensionsManagement;
    }

    @Override
    public InterfacesManagement getInterfacesManagement() {
        return interfacesManagement;
    }

    @Override
    public SystemPropertiesManagement getSystemPropertiesManagement() {
        return systemPropertiesManagement;
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
        public StandaloneServerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedStandaloneServerConfiguration(configFile.getFileName().toString(), server);
        }
    }

    @Override
    public <C extends ManageableNode> List<C> findChildren(Select<C> select) throws IOException {
        List<C> result = super.findChildren(select);
        if (result == null) {
            result = new ArrayList<>();
        }
        if (select.getType().isInstance(ManageableResources.class)) {
            if (select.getType() == DeploymentsManagement.class) {
                C c = (C) deploymentsManagement;
                if (select.test(c)) {
                    result.add(c);
                }
            } else if (select.getType() == InterfacesManagement.class) {
                C c = (C) interfacesManagement;
                if (select.test(c)) {
                    result.add(c);
                }
            }
        } else if (select.getType() == SecurityRealmsManagement.class) {
            C c = (C) securityRealmsManagement;
            if (select.test(c)) {
                result.add(c);
            }
        } else if (select.getType() == SubsystemsManagement.class) {
            C c = (C) subsystemsManagement;
            if (select.test(c)) {
                result.add(c);
            }
        } else if (select.getType() == ManagementInterfacesManagement.class) {
            C c = (C) managementInterfacesManagement;
            if (select.test(c)) {
                result.add(c);
            }
        }
        return result;
    }
}
