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

import org.jboss.migration.core.Server;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

/**
 * @author emmartins
 * @param <SS> source server type
 * @param <SC> source config type
 */
public class MigrationBuilders<SS extends Server, SC> {
    public DomainMigration.Builder<SS> domainBuilder() {
        return new DomainMigration.Builder<>();
    }
    public SubsystemsMigration.Builder<SC> subsystemsMigrationBuilder() {
        return new SubsystemsMigration.Builder<>();
    }
    public StandaloneServerConfigurationMigration.Builder<SC> standaloneConfigurationMigrationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new StandaloneServerConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }
    public DomainConfigurationMigration.Builder<SC> domainConfigurationMigrationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new DomainConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }
    public HostConfigurationMigration.Builder<SC> hostConfigurationMigrationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new HostConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }
    public HostMigration.Builder<SC> hostMigrationBuilder() {
        return new HostMigration.Builder<>();
    }
    public ProfileMigration.Builder<SC> profileMigrationBuilder() {
        return new ProfileMigration.Builder<>();
    }
    public SocketBindingGroupMigration.Builder<SC> socketBindingGroupMigrationBuilder() {
        return new SocketBindingGroupMigration.Builder<>();
    }
    public SocketBindingsMigration.Builder<SC> socketBindingsMigrationBuilder() {
        return new SocketBindingsMigration.Builder<>();
    }
    public ManagementInterfacesMigration.Builder<SC> managementInterfacesMigrationBuilder() {
        return new ManagementInterfacesMigration.Builder<>();
    }
    public InterfacesMigration.Builder<SC> interfacesMigrationBuilder() {
        return new InterfacesMigration.Builder<>();
    }
    public JVMsMigration.Builder<SC> jvmsMigrationBuilder() {
        return new JVMsMigration.Builder<>();
    }
    public ServerGroupMigration.Builder<SC> serverGroupMigrationBuilder() {
        return new ServerGroupMigration.Builder<>();
    }
}
