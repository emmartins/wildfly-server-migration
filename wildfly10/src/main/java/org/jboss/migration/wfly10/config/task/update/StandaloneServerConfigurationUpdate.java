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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.core.JBossServer;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.config.task.DeploymentsMigration;
import org.jboss.migration.wfly10.config.task.ManagementInterfacesMigration;
import org.jboss.migration.wfly10.config.task.SecurityRealmsMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingGroupMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingGroupsMigration;
import org.jboss.migration.wfly10.config.task.StandaloneServerConfigurationMigration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

/**
 * @author emmartins
 */
public class StandaloneServerConfigurationUpdate<S extends JBossServer<S>> extends StandaloneServerConfigurationMigration<ServerPath<S>> {

    protected StandaloneServerConfigurationUpdate(StandaloneServerConfigurationMigration.Builder<ServerPath<S>> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> {

        private DeploymentsMigration<ServerPath<S>> deploymentsMigration;
        private ManagementInterfacesMigration<ServerPath<S>> managementInterfacesMigration;
        private SecurityRealmsMigration<ServerPath<S>> securityRealmsMigration;
        private SocketBindingGroupsMigration<ServerPath<S>> socketBindingGroupsMigration;
        private SubsystemsMigration<ServerPath<S>> subsystemsMigration;

        public Builder<S> deploymentsMigration(DeploymentsMigration<ServerPath<S>> deploymentsMigration) {
            this.deploymentsMigration = deploymentsMigration;
            return this;
        }

        public Builder<S> deploymentsMigration(DeploymentsMigration.Builder<ServerPath<S>> deploymentsMigrationBuilder) {
            return deploymentsMigration(deploymentsMigrationBuilder.build());
        }

        public Builder<S> managementInterfacesMigration(ManagementInterfacesMigration<ServerPath<S>> managementInterfacesMigration) {
            this.managementInterfacesMigration = managementInterfacesMigration;
            return this;
        }

        public Builder<S> managementInterfacesMigration(ManagementInterfacesMigration.Builder<ServerPath<S>> managementInterfacesMigrationBuilder) {
            return managementInterfacesMigration(managementInterfacesMigrationBuilder.build());
        }

        public Builder<S> securityRealmsMigration(SecurityRealmsMigration<ServerPath<S>> securityRealmsMigration) {
            this.securityRealmsMigration = securityRealmsMigration;
            return this;
        }

        public Builder<S> securityRealmsMigration(SecurityRealmsMigration.Builder<ServerPath<S>> securityRealmsMigrationBuilder) {
            return securityRealmsMigration(securityRealmsMigrationBuilder.build());
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupsMigration<ServerPath<S>> socketBindingGroupsMigration) {
            this.socketBindingGroupsMigration = socketBindingGroupsMigration;
            return this;
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupsMigration.Builder<ServerPath<S>> socketBindingGroupsMigrationBuilder) {
            return socketBindingGroupsMigration(socketBindingGroupsMigrationBuilder.build());
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupMigration<ServerPath<S>> socketBindingGroupMigration) {
            return socketBindingGroupsMigration(SocketBindingGroupsMigration.from(socketBindingGroupMigration));
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupMigration.Builder<ServerPath<S>> socketBindingGroupMigrationBuilder) {
            return socketBindingGroupsMigration(socketBindingGroupMigrationBuilder.build());
        }

        public Builder<S> subsystemsMigration(SubsystemsMigration<ServerPath<S>> subsystemsMigration) {
            this.subsystemsMigration = subsystemsMigration;
            return this;
        }

        public Builder<S> subsystemsMigration(SubsystemsMigration.Builder<ServerPath<S>> subsystemsMigrationBuilder) {
            return subsystemsMigration(subsystemsMigrationBuilder.build());
        }

        public StandaloneServerConfigurationUpdate<S> build() {
            final StandaloneServerConfigurationMigration.Builder<ServerPath<S>> builder = new StandaloneServerConfigurationMigration.Builder<>(new CopySourceXMLConfiguration<S>());
            if (subsystemsMigration != null) {
                builder.addSubsystemsMigration(subsystemsMigration);
            }
            if (deploymentsMigration != null) {
                builder.addDeploymentsMigration(deploymentsMigration);
            } else {
                builder.addDeploymentsMigration(new RemoveDeployment<ServerPath<S>>().buildDeploymentsMigration());
            }
            if (managementInterfacesMigration != null) {
                builder.addManagementInterfacesMigration(managementInterfacesMigration);
            }
            if (socketBindingGroupsMigration != null) {
                builder.addSocketBindingGroupsMigration(socketBindingGroupsMigration);
            }
            if (securityRealmsMigration != null) {
                builder.addSecurityRealmsMigration(securityRealmsMigration);
            } else {
                builder.addSecurityRealmsMigration(new MigrateCompatibleSecurityRealm<S>().buildSecurityRealmsMigration());
            }
            return new StandaloneServerConfigurationUpdate<S>(builder);
        }
    }
}
