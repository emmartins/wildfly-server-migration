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
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostResource;
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.management.ServerGroupResource;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.HostController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class EmbeddedHostControllerConfiguration extends AbstractManageableServerConfiguration implements HostControllerConfiguration {

    private final String domainConfig;
    private final String hostConfig;
    private HostController hostController;
    private final RootResource rootResource;

    protected EmbeddedHostControllerConfiguration(String domainConfig, String hostConfig, WildFlyServer10 server) {
        super(server);
        this.domainConfig = domainConfig;
        this.hostConfig = hostConfig;
        rootResource = new RootResource(this);
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
    public RootResource getRootResource() {
        return rootResource;
    }

    protected static class RootResource extends AbstractManageableServerConfiguration.RootResource implements HostControllerConfiguration.RootResource {

        private final DeploymentResourceImpl.Factory deploymentResources;
        private final HostResourceImpl.Factory hostResources;
        private final ProfileResourceImpl.Factory profileResources;
        private final ServerGroupResourceImpl.Factory serverGroupResources;

        protected RootResource(HostControllerConfiguration serverConfiguration) {
            super("", PathAddress.EMPTY_ADDRESS, serverConfiguration);
            deploymentResources = new DeploymentResourceImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(deploymentResources);
            hostResources = new HostResourceImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(hostResources);
            profileResources = new ProfileResourceImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(profileResources);
            serverGroupResources = new ServerGroupResourceImpl.Factory(getResourcePathAddress(), this, serverConfiguration);
            addChildResourceFactory(serverGroupResources);
        }

        @Override
        public DeploymentResource getDeploymentResource(String resourceName) throws IOException {
            return deploymentResources.getResource(resourceName);
        }

        @Override
        public List<DeploymentResource> getDeploymentResources() throws IOException {
            return deploymentResources.getResources();
        }

        @Override
        public Set<String> getDeploymentResourceNames() throws IOException {
            return deploymentResources.getResourceNames();
        }

        @Override
        public PathAddress getDeploymentResourcePathAddress(String resourceName) {
            return deploymentResources.getResourcePathAddress(resourceName);
        }

        @Override
        public void removeDeploymentResource(String resourceName) throws IOException {
            deploymentResources.removeResource(resourceName);
        }

        @Override
        public HostResource getHostResource(String resourceName) throws IOException {
            return hostResources.getResource(resourceName);
        }

        @Override
        public List<HostResource> getHostResources() throws IOException {
            return hostResources.getResources();
        }

        @Override
        public Set<String> getHostResourceNames() throws IOException {
            return hostResources.getResourceNames();
        }

        @Override
        public PathAddress getHostResourcePathAddress(String resourceName) {
            return hostResources.getResourcePathAddress(resourceName);
        }

        @Override
        public void removeHostResource(String resourceName) throws IOException {
            hostResources.removeResource(resourceName);
        }

        @Override
        public ProfileResource getProfileResource(String resourceName) throws IOException {
            return profileResources.getResource(resourceName);
        }

        @Override
        public List<ProfileResource> getProfileResources() throws IOException {
            return profileResources.getResources();
        }

        @Override
        public Set<String> getProfileResourceNames() throws IOException {
            return profileResources.getResourceNames();
        }

        @Override
        public PathAddress getProfileResourcePathAddress(String resourceName) {
            return profileResources.getResourcePathAddress(resourceName);
        }

        @Override
        public void removeProfileResource(String resourceName) throws IOException {
            profileResources.removeResource(resourceName);
        }

        @Override
        public ServerGroupResource getServerGroupResource(String resourceName) throws IOException {
            return serverGroupResources.getResource(resourceName);
        }

        @Override
        public List<ServerGroupResource> getServerGroupResources() throws IOException {
            return serverGroupResources.getResources();
        }

        @Override
        public Set<String> getServerGroupResourceNames() throws IOException {
            return serverGroupResources.getResourceNames();
        }

        @Override
        public PathAddress getServerGroupResourcePathAddress(String resourceName) {
            return serverGroupResources.getResourcePathAddress(resourceName);
        }

        @Override
        public void removeServerGroupResource(String resourceName) throws IOException {
            serverGroupResources.removeResource(resourceName);
        }
    }
}
