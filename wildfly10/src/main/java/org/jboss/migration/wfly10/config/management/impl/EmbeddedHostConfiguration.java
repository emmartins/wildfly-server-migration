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
import org.jboss.migration.wfly10.config.management.JvmResources;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResources;
import org.jboss.migration.wfly10.config.management.SecurityRealmResources;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.HostMigration;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedHostConfiguration extends AbstractManageableServerConfiguration implements HostConfiguration {

    private final String host;
    private final HostControllerConfiguration hostController;
    private final SubsystemResources subsystemResources;
    private final SecurityRealmResources securityRealmResources;
    private final ManagementInterfaceResources managementInterfaceResources;
    private final JvmResources jvmResources;

    public EmbeddedHostConfiguration(HostControllerConfiguration hostController, String host) {
        super(hostController.getServer(), pathAddress(pathElement(HOST, host)));
        this.hostController = hostController;
        this.host = host;
        this.subsystemResources = new SubsystemResourcesImpl(pathAddress, this);
        addChildResources(subsystemResources);
        this.jvmResources = new JvmResourcesImpl(pathAddress, this);
        addChildResources(jvmResources);
        final PathAddress managementCoreServicePathAddress = pathAddress.append(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmResources = new SecurityRealmResourcesImpl(managementCoreServicePathAddress, this);
        addChildResources(securityRealmResources);
        this.managementInterfaceResources = new ManagementInterfaceResourcesImpl(managementCoreServicePathAddress, this);
        addChildResources(managementInterfaceResources);
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        writeConfiguration();
    }

    public SecurityRealmResources getSecurityRealmResources() {
        return securityRealmResources;
    }

    public SubsystemResources getSubsystemResources() {
        return subsystemResources;
    }

    public JvmResources getJvmResources() {
        return jvmResources;
    }

    public ManagementInterfaceResources getManagementInterfaceResources() {
        return managementInterfaceResources;
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
}
