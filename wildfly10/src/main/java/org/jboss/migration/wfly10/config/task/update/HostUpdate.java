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
import org.jboss.migration.wfly10.config.task.HostMigration;
import org.jboss.migration.wfly10.config.task.JVMsMigration;
import org.jboss.migration.wfly10.config.task.ManagementInterfacesMigration;
import org.jboss.migration.wfly10.config.task.SecurityRealmsMigration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

/**
 * @author emmartins
 */
public class HostUpdate<S extends JBossServer<S>> extends HostMigration<ServerPath<S>> {

    protected HostUpdate(HostMigration.Builder<ServerPath<S>> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> {

        private SubsystemsMigration<ServerPath<S>> subsystemsMigration;
        private SecurityRealmsMigration<ServerPath<S>> securityRealmsMigration;
        private ManagementInterfacesMigration<ServerPath<S>> managementInterfacesMigration;
        private JVMsMigration<ServerPath<S>> jvMsMigration;

        public Builder<S> subsystemsMigration(SubsystemsMigration<ServerPath<S>> subsystemsMigration) {
            this.subsystemsMigration = subsystemsMigration;
            return this;
        }

        public Builder<S> subsystemsMigration(SubsystemsMigration.Builder<ServerPath<S>> subsystemsMigrationBuilder) {
            return subsystemsMigration(subsystemsMigrationBuilder.build());
        }

        public Builder<S> securityRealmsMigration(SecurityRealmsMigration<ServerPath<S>> securityRealmsMigration) {
            this.securityRealmsMigration = securityRealmsMigration;
            return this;
        }

        public Builder<S> securityRealmsMigration(SecurityRealmsMigration.Builder<ServerPath<S>> securityRealmsMigrationBuilder) {
            return securityRealmsMigration(securityRealmsMigrationBuilder.build());
        }

        public Builder<S> managementInterfacesMigration(ManagementInterfacesMigration<ServerPath<S>> managementInterfacesMigration) {
            this.managementInterfacesMigration = managementInterfacesMigration;
            return this;
        }

        public Builder<S> managementInterfacesMigration(ManagementInterfacesMigration.Builder<ServerPath<S>> managementInterfacesMigrationBuilder) {
            return managementInterfacesMigration(managementInterfacesMigrationBuilder.build());
        }

        public Builder<S> jvMsMigration(JVMsMigration<ServerPath<S>> jvMsMigration) {
            this.jvMsMigration = jvMsMigration;
            return this;
        }

        public Builder<S> jvMsMigration(JVMsMigration.Builder<ServerPath<S>> jvMsMigrationBuilder) {
            return jvMsMigration(jvMsMigrationBuilder.build());
        }

        public HostUpdate<S> build() {
            final HostMigration.Builder<ServerPath<S>> builder = new HostMigration.Builder<>();
            if (subsystemsMigration != null) {
                builder.addSubsystemsMigration(subsystemsMigration);
            }
            if (securityRealmsMigration != null) {
                builder.addSecurityRealmsMigration(securityRealmsMigration);
            } else {
                builder.addSecurityRealmsMigration(new MigrateCompatibleSecurityRealm<S>().buildSecurityRealmsMigration());
            }
            if (managementInterfacesMigration != null) {
                builder.addManagementInterfacesMigration(managementInterfacesMigration);
            }
            if (jvMsMigration != null) {
                builder.addJVMsMigration(jvMsMigration);
            }
            return new HostUpdate<>(builder);
        }
    }
}
