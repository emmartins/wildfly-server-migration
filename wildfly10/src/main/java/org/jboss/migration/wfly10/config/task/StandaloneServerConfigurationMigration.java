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
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedStandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

import java.nio.file.Path;

/**
 * Standalone config migration.
 * @author emmartins
 */
public class StandaloneServerConfigurationMigration<S> extends ServerConfigurationMigration<S, StandaloneServerConfiguration> {

    protected StandaloneServerConfigurationMigration(Builder builder) {
        super(builder);
    }

    public static class Builder<S> extends ServerConfigurationMigration.Builder<Builder, S, StandaloneServerConfiguration> {

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            super("standalone", xmlConfigurationProvider);
            manageableConfigurationProvider(new EmbeddedStandaloneServerConfiguration.ConfigFileMigrationFactory());
        }

        @Override
        public StandaloneServerConfigurationMigration<S> build() {
            return new StandaloneServerConfigurationMigration<>(this);
        }

        public Builder<S> addDeploymentsMigration(final DeploymentsMigration<S> deploymentsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return deploymentsMigration.getTask(source, configuration.getDeploymentsManagement());
                }
            });
        }

        public ServerConfigurationMigration.Builder addInterfacesMigration(final InterfacesMigration<S> interfacesMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return interfacesMigration.getTask(source, configuration.getInterfacesManagement());
                }
            });
        }

        public ServerConfigurationMigration.Builder addManagementInterfacesMigration(final ManagementInterfacesMigration<S> managementInterfacesMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return managementInterfacesMigration.getTask(source, configuration.getManagementInterfacesManagement());
                }
            });
        }

        public ServerConfigurationMigration.Builder addSecurityRealmsMigration(final SecurityRealmsMigration<S> securityRealmsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return securityRealmsMigration.getTask(source, configuration.getSecurityRealmsManagement());
                }
            });
        }

        public ServerConfigurationMigration.Builder addSocketBindingGroupsMigration(final SocketBindingGroupsMigration<S> socketBindingGroupsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return socketBindingGroupsMigration.getTask(source, configuration.getSocketBindingGroupsManagement());
                }
            });
        }

        public Builder<S> addSubsystemsMigration(final SubsystemsMigration<S> subsystemsMigration) {
            addXMLConfigurationSubtaskFactory(new XMLConfigurationSubtaskFactory<S>() {
                @Override
                public ServerMigrationTask getXMLConfigurationSubtask(S source, Path xmlConfigurationPath, WildFly10Server target) {
                    return subsystemsMigration.getXMLConfigurationTask(source, xmlConfigurationPath, target);
                }
            });
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return subsystemsMigration.getSubsystemsManagementTask(source, configuration.getSubsystemsManagement());
                }
            });
        }
    }
}
