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
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.management.SystemPropertiesManagement;
import org.jboss.migration.wfly10.config.task.HostMigration;

import java.io.IOException;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedHostConfiguration extends AbstractManageableServerConfiguration implements HostConfiguration {

    private final String host;
    private final HostControllerConfiguration hostController;
    private final SubsystemsManagement subsystemsManagement;
    private final SecurityRealmsManagement securityRealmsManagement;
    private final ExtensionsManagement extensionsManagement;
    private final InterfacesManagement interfacesManagement;
    private final ManagementInterfacesManagement managementInterfacesManagement;
    private final JVMsManagement JVMsManagement;
    private final SocketBindingGroupsManagement socketBindingGroupsManagement;
    private final SystemPropertiesManagement systemPropertiesManagement;

    private final PathAddress hostPathAddress;

    public EmbeddedHostConfiguration(HostControllerConfiguration hostController, String host) {
        super(hostController.getServer());
        this.hostController = hostController;
        this.host = host;
        this.hostPathAddress = pathAddress(pathElement(HOST, host));
        this.extensionsManagement = new ExtensionsManagementImpl(hostPathAddress, this){
            @Override
            public Set<String> getSubsystems() throws IOException {
                return getSubsystemsManagement().getResourceNames();
            }
        };
        this.interfacesManagement = new InterfacesManagementImpl(hostPathAddress, this);
        this.subsystemsManagement = new SubsystemsManagementImpl(hostPathAddress, this);
        this.JVMsManagement = new JVMsManagementImpl(hostPathAddress, this);
        this.socketBindingGroupsManagement = new SocketBindingGroupsManagementImpl(hostPathAddress, this);
        this.systemPropertiesManagement = new SystemPropertiesManagementImpl(hostPathAddress, this);

        final PathAddress managementCoreServicePathAddress = hostPathAddress.append(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmsManagement = new SecurityRealmsManagementImpl(managementCoreServicePathAddress, this);
        this.managementInterfacesManagement = new ManagementInterfacesManagementImpl(managementCoreServicePathAddress, this);
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        writeConfiguration();
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
    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }

    @Override
    public SocketBindingGroupsManagement getSocketBindingGroupsManagement() {
        return socketBindingGroupsManagement;
    }

    @Override
    public SystemPropertiesManagement getSystemPropertiesManagement() {
        return systemPropertiesManagement;
    }

    public JVMsManagement getJVMsManagement() {
        return JVMsManagement;
    }

    @Override
    public ManagementInterfacesManagement getManagementInterfacesManagement() {
        return managementInterfacesManagement;
    }

    @Override
    public PathAddress getPathAddress() {
        return hostPathAddress;
    }

    public static class HostConfigFileMigrationFactory implements HostMigration.HostConfigurationProvider {
        @Override
        public EmbeddedHostConfiguration getHostConfiguration(String host, HostControllerConfiguration hostController) throws Exception {
            return new EmbeddedHostConfiguration(hostController, host);
        }
    }
}
