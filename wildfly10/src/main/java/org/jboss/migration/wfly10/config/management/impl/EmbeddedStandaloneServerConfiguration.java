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
import org.jboss.migration.wfly10.config.management.DeploymentResources;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResources;
import org.jboss.migration.wfly10.config.management.SecurityRealmResources;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.nio.file.Path;

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
    private final DeploymentResources deploymentResources;
    private final ManagementInterfaceResources managementInterfaceResources;
    private final SecurityRealmResources securityRealmResources;
    private final SubsystemResources subsystemResources;

    public EmbeddedStandaloneServerConfiguration(String config, WildFlyServer10 server) {
        super(server, PathAddress.EMPTY_ADDRESS);
        this.config = config;
        this.deploymentResources = new DeploymentResourcesImpl(pathAddress, this);
        addChildResources(deploymentResources);
        this.subsystemResources = new SubsystemResourcesImpl(pathAddress, this);
        addChildResources(subsystemResources);
        final PathAddress managementCoreServicePathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmResources = new SecurityRealmResourcesImpl(managementCoreServicePathAddress, this);
        addChildResources(securityRealmResources);
        this.managementInterfaceResources = new ManagementInterfaceResourcesImpl(managementCoreServicePathAddress, this);
        addChildResources(managementInterfaceResources);
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

    public DeploymentResources getDeploymentResources() {
        return deploymentResources;
    }

    public ManagementInterfaceResources getManagementInterfaceResources() {
        return managementInterfaceResources;
    }

    public SecurityRealmResources getSecurityRealmResources() {
        return securityRealmResources;
    }

    public SubsystemResources getSubsystemResources() {
        return subsystemResources;
    }

    public static class ConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public StandaloneServerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedStandaloneServerConfiguration(configFile.getFileName().toString(), server);
        }
    }
}
