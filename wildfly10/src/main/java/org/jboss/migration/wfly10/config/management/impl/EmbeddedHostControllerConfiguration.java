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
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostResources;
import org.jboss.migration.wfly10.config.management.ProfileResources;
import org.jboss.migration.wfly10.config.management.ServerGroupResources;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.HostController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class EmbeddedHostControllerConfiguration extends AbstractManageableServerConfiguration implements HostControllerConfiguration {

    private final String domainConfig;
    private final String hostConfig;
    private HostController hostController;
    private final DeploymentResources deploymentResources;
    private final HostResources hostResources;
    private final ProfileResources profileResources;
    private final ServerGroupResources serverGroupResources;

    protected EmbeddedHostControllerConfiguration(String domainConfig, String hostConfig, WildFlyServer10 server) {
        super(server, PathAddress.EMPTY_ADDRESS);
        this.domainConfig = domainConfig;
        this.hostConfig = hostConfig;
        this.deploymentResources = new DeploymentResourcesImpl(pathAddress, this);
        addChildResources(deploymentResources);
        this.hostResources = new HostResourcesImpl(pathAddress, this);
        addChildResources(hostResources);
        this.profileResources = new ProfileResourcesImpl(pathAddress, this);
        addChildResources(profileResources);
        this.serverGroupResources = new ServerGroupResourcesImpl(pathAddress, this);
        addChildResources(serverGroupResources);
    }

    @Override
    protected ModelControllerClient startConfiguration() {

        final List<String> cmds = new ArrayList<>();
        if (domainConfig != null) {
            cmds.add("--domain-config="+ domainConfig);
        }
        if (hostConfig != null) {
            cmds.add("--host-config="+ hostConfig);
        }
        cmds.add("--admin-only");
        final String[] systemPackages = {"org.jboss.logmanager"};
        hostController = EmbeddedProcessFactory.createHostController(getServer().getBaseDir().toString(), null, systemPackages, cmds.toArray(new String[cmds.size()]));
        try {
            hostController.start();
        } catch (EmbeddedProcessStartException e) {
            throw new RuntimeException(e);
        }
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        if (hostConfig == null) {
            writeConfiguration();
        }
        hostController.stop();
        hostController = null;
    }

    public DeploymentResources getDeploymentResources() {
        return deploymentResources;
    }

    public HostResources getHostResources() {
        return hostResources;
    }

    public ProfileResources getProfileResources() {
        return profileResources;
    }

    public ServerGroupResources getServerGroupResources() {
        return serverGroupResources;
    }

    public static class DomainConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public HostControllerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedHostControllerConfiguration(configFile.getFileName().toString(), null, server);
        }
    }

    public static class HostConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public HostControllerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedHostControllerConfiguration(null, configFile.getFileName().toString(), server);
        }
    }
}
