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
import org.jboss.migration.wfly10.config.task.DomainConfigurationMigration;
import org.jboss.migration.wfly10.config.task.InterfacesMigration;
import org.jboss.migration.wfly10.config.task.ProfileMigration;
import org.jboss.migration.wfly10.config.task.ProfilesMigration;
import org.jboss.migration.wfly10.config.task.ServerGroupMigration;
import org.jboss.migration.wfly10.config.task.ServerGroupsMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingGroupMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingGroupsMigration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

/**
 * @author emmartins
 */
public class DomainConfigurationUpdate<S extends JBossServer<S>> extends DomainConfigurationMigration<ServerPath<S>> {

    protected DomainConfigurationUpdate(DomainConfigurationMigration.Builder<ServerPath<S>> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> {

        private DeploymentsMigration<ServerPath<S>> deploymentsMigration;
        private ProfilesMigration<ServerPath<S>> profilesMigration;
        private ServerGroupsMigration<ServerPath<S>> serverGroupsMigration;
        private SocketBindingGroupsMigration<ServerPath<S>> socketBindingGroupsMigration;
        private InterfacesMigration<ServerPath<S>> interfacesMigration;
        private SubsystemsMigration<ServerPath<S>> subsystemsMigration;

        public Builder<S> deploymentsMigration(DeploymentsMigration<ServerPath<S>> deploymentsMigration) {
            this.deploymentsMigration = deploymentsMigration;
            return this;
        }

        public Builder<S> profilesMigration(ProfilesMigration<ServerPath<S>> profilesMigration) {
            this.profilesMigration = profilesMigration;
            return this;
        }

        public Builder<S> profilesMigration(ProfileMigration<ServerPath<S>> profileMigration) {
            return profilesMigration(ProfilesMigration.from(profileMigration));
        }

        public Builder<S> profilesMigration(SubsystemsMigration<ServerPath<S>> subsystemsMigration) {
            return profilesMigration(new ProfileMigration.Builder<ServerPath<S>>().addSubsystemsMigration(subsystemsMigration).build());
        }

        public Builder<S> subsystemsMigration(SubsystemsMigration<ServerPath<S>> subsystemsMigration) {
            this.subsystemsMigration = subsystemsMigration;
            return this;
        }

        public Builder<S> serverGroupsMigration(ServerGroupsMigration<ServerPath<S>> serverGroupsMigration) {
            this.serverGroupsMigration = serverGroupsMigration;
            return this;
        }

        public Builder<S> serverGroupsMigration(ServerGroupMigration<ServerPath<S>> serverGroupMigration) {
            return serverGroupsMigration(ServerGroupsMigration.from(serverGroupMigration));
        }

        public Builder<S> serverGroupsMigration(ServerGroupMigration.Builder<ServerPath<S>> serverGroupMigrationBuilder) {
            return serverGroupsMigration(serverGroupMigrationBuilder.build());
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupsMigration<ServerPath<S>> socketBindingGroupsMigration) {
            this.socketBindingGroupsMigration = socketBindingGroupsMigration;
            return this;
        }

        public Builder<S> socketBindingGroupsMigration(SocketBindingGroupMigration<ServerPath<S>> socketBindingGroupMigration) {
            return socketBindingGroupsMigration(SocketBindingGroupsMigration.from(socketBindingGroupMigration));
        }

        public Builder<S> interfacesMigration(InterfacesMigration<ServerPath<S>> interfacesMigration) {
            this.interfacesMigration = interfacesMigration;
            return this;
        }

        public Builder<S> interfacesMigration(InterfacesMigration.Builder<ServerPath<S>> interfacesMigrationBuilder) {
            return this.interfacesMigration(interfacesMigrationBuilder.build());
        }

        public DomainConfigurationUpdate<S> build() {
            final DomainConfigurationMigration.Builder<ServerPath<S>> builder = new DomainConfigurationMigration.Builder<>(new CopySourceXMLConfiguration<S>());
            if (subsystemsMigration != null) {
                builder.addSubsystemsMigration(subsystemsMigration);
            }
            if (profilesMigration != null) {
                builder.addProfilesMigration(profilesMigration);
            }
            if (deploymentsMigration != null) {
                builder.addDeploymentsMigration(deploymentsMigration);
            } else {
                builder.addDeploymentsMigration(new RemoveDeployment<ServerPath<S>>().buildDeploymentsMigration());
            }
            if (serverGroupsMigration != null) {
                builder.addServerGroupsMigration(serverGroupsMigration);
            }
            if (socketBindingGroupsMigration != null) {
                builder.addSocketBindingGroupsMigration(socketBindingGroupsMigration);
            }
            if (interfacesMigration != null) {
                builder.addInterfacesMigration(interfacesMigration);
            }
            return new DomainConfigurationUpdate<>(builder);
        }
    }
}
