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
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.HostMigration;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedHostConfiguration extends AbstractManageableServerConfiguration implements HostConfiguration {

    private final HostControllerConfiguration hostController;
    private final RootResource rootResource;

    public EmbeddedHostConfiguration(HostControllerConfiguration hostController, String host) {
        super(hostController.getServer());
        this.hostController = hostController;
        this.rootResource = new RootResource(host, this);
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        writeConfiguration();
    }

    public static class HostConfigFileMigrationFactory implements HostMigration.HostConfigurationProvider {
        @Override
        public EmbeddedHostConfiguration getHostConfiguration(String host, HostControllerConfiguration hostController) throws Exception {
            return new EmbeddedHostConfiguration(hostController, host);
        }
    }

    @Override
    public RootResource getRootResource() {
        return rootResource;
    }

    protected static class RootResource extends AbstractManageableServerConfiguration.RootResource implements HostConfiguration.RootResource {

        private final JvmResourceImpl.Factory jvmResources;
        private final ManagementInterfaceResourceImpl.Factory managementInterfaceResources;
        private final SecurityRealmResourceImpl.Factory securityRealmResources;
        private final SubsystemConfigurationImpl.Factory subsystemResources;

        protected RootResource(String host, HostConfiguration serverConfiguration) {
            super(host, pathAddress(HOST, host), serverConfiguration);
            jvmResources = new JvmResourceImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(jvmResources);
            subsystemResources = new SubsystemConfigurationImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(subsystemResources);
            final PathAddress managementCoreServicePathAddress = getResourcePathAddress().append(CORE_SERVICE, MANAGEMENT);
            managementInterfaceResources = new ManagementInterfaceResourceImpl.Factory(managementCoreServicePathAddress, this, serverConfiguration);
            addChildResourceFactory(managementInterfaceResources);
            securityRealmResources = new SecurityRealmResourceImpl.Factory(managementCoreServicePathAddress, this, serverConfiguration);
            addChildResourceFactory(securityRealmResources);
        }

        @Override
        public JvmResource getJvmResource(String resourceName) throws IOException {
            return jvmResources.getResource(resourceName);
        }

        @Override
        public List<JvmResource> getJvmResources() throws IOException {
            return jvmResources.getResources();
        }

        @Override
        public Set<String> getJvmResourceNames() throws IOException {
            return jvmResources.getResourceNames();
        }

        @Override
        public PathAddress getJvmResourcePathAddress(String resourceName) {
            return jvmResources.getResourcePathAddress(resourceName);
        }

        @Override
        public void removeJvmResource(String resourceName) throws IOException {
            jvmResources.removeResource(resourceName);
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
        public List<SubsystemConfiguration> getSubsystemConfiguration() throws IOException {
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
    }
}
