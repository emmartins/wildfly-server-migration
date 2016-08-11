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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostControllerConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

import java.nio.file.Path;

/**
 * Builder for a domain config migration.
 * @author emmartins
 */
public class DomainConfigurationMigration<S> extends ServerConfigurationMigration<S, HostControllerConfiguration> {

    public static final String DOMAIN = "domain";

    protected DomainConfigurationMigration(Builder builder) {
        super(builder);
    }

    public static class Builder<S> extends ServerConfigurationMigration.Builder<Builder, S, HostControllerConfiguration> {

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            super(DOMAIN, xmlConfigurationProvider);
            manageableConfigurationProvider(new EmbeddedHostControllerConfiguration.DomainConfigFileMigrationFactory());
        }

        @Override
        public DomainConfigurationMigration<S> build() {
            return new DomainConfigurationMigration<>(this);
        }

        public Builder<S> addDeploymentsMigration(final DeploymentsMigration<S> deploymentsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return deploymentsMigration.getTask(source, configuration.getDeploymentsManagement());
                }
            });
        }

        public Builder<S> addProfilesMigration(final ProfilesMigration<S> profilesMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return profilesMigration.getTask(source, configuration.getProfilesManagement());
                }
            });
        }

        public Builder<S> addSubsystemsMigration(final SubsystemsMigration<S> subsystemsMigration) {
            return addXMLConfigurationSubtaskFactory(new XMLConfigurationSubtaskFactory<S>() {
                @Override
                public ServerMigrationTask getXMLConfigurationSubtask(S source, Path xmlConfigurationPath, WildFly10Server target) {
                    return subsystemsMigration.getXMLConfigurationTask(source, xmlConfigurationPath, target);
                }
            });
        }

        public Builder<S> addServerGroupsMigration(final ServerGroupsMigration<S> serverGroupsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return serverGroupsMigration.getTask(source, configuration.getServerGroupsManagement());
                }
            });
        }

        public Builder<S> addSocketBindingGroupsMigration(final SocketBindingGroupsMigration<S> socketBindingGroupsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return socketBindingGroupsMigration.getTask(source, configuration.getSocketBindingGroupsManagement());
                }
            });
        }

        public Builder<S> addInterfacesMigration(final InterfacesMigration<S> interfacesMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return interfacesMigration.getTask(source, configuration.getInterfacesManagement());
                }
            });
        }
    }
}
