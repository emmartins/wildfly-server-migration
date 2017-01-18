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
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.HostMigration;

import java.io.IOException;
import java.util.List;

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
    private final ManagementInterfacesManagement managementInterfacesManagement;
    private final JVMsManagement JVMsManagement;

    public EmbeddedHostConfiguration(HostControllerConfiguration hostController, String host) {
        super(hostController.getServer(), pathAddress(pathElement(HOST, host)));
        this.hostController = hostController;
        this.host = host;
        this.subsystemsManagement = new SubsystemsManagementImpl(pathAddress, this);
        this.JVMsManagement = new JVMsManagementImpl(pathAddress, this);
        final PathAddress managementCoreServicePathAddress = pathAddress.append(pathElement(CORE_SERVICE, MANAGEMENT));
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
    public SecurityRealmsManagement getSecurityRealmsManagement() {
        return securityRealmsManagement;
    }

    @Override
    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
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
        return pathAddress;
    }

    public static class HostConfigFileMigrationFactory implements HostMigration.HostConfigurationProvider {
        @Override
        public EmbeddedHostConfiguration getHostConfiguration(String host, HostControllerConfiguration hostController) throws Exception {
            return new EmbeddedHostConfiguration(hostController, host);
        }
    }

    @Override
    public <T extends ManageableResources> List<T> findResources(Class<T> resourcesType) throws IOException {
        final List<T> result = super.findResources(resourcesType);
        findResources(getJVMsManagement(), resourcesType, result);
        findResources(getManagementInterfacesManagement(), resourcesType, result);
        findResources(getSecurityRealmsManagement(), resourcesType, result);
        findResources(getSubsystemsManagement(), resourcesType, result);
        return result;
    }
}
