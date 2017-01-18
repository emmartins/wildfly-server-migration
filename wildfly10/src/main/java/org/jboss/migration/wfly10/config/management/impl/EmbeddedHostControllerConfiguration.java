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
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostsManagement;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.HostController;

import java.io.IOException;
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
    private final DeploymentsManagement deploymentsManagement;
    private final HostsManagement hostsManagement;
    private final ProfilesManagement profilesManagement;
    private final ServerGroupsManagement serverGroupsManagement;

    protected EmbeddedHostControllerConfiguration(String domainConfig, String hostConfig, WildFlyServer10 server) {
        super(server, PathAddress.EMPTY_ADDRESS);
        this.domainConfig = domainConfig;
        this.hostConfig = hostConfig;
        this.deploymentsManagement = new DeploymentsManagementImpl(pathAddress, this);
        this.hostsManagement = new HostsManagementImpl(pathAddress, this);
        this.profilesManagement = new ProfilesManagementImpl(pathAddress, this);
        this.serverGroupsManagement = new ServerGroupsManagementImpl(pathAddress, this);
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

    public DeploymentsManagement getDeploymentsManagement() {
        return deploymentsManagement;
    }

    public HostsManagement getHostsManagement() {
        return hostsManagement;
    }

    @Override
    public ProfilesManagement getProfilesManagement() {
        return profilesManagement;
    }

    @Override
    public ServerGroupsManagement getServerGroupsManagement() {
        return serverGroupsManagement;
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

    @Override
    public <T extends ManageableResources> List<T> findResources(Class<T> resourcesType) throws IOException {
        final List<T> result = super.findResources(resourcesType);
        findResources(getDeploymentsManagement(), resourcesType, result);
        findResources(getHostsManagement(), resourcesType, result);
        findResources(getProfilesManagement(), resourcesType, result);
        findResources(getServerGroupsManagement(), resourcesType, result);
        return result;
    }
}
