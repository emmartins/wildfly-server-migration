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

    public SubsystemsMigration.Builder<SC> subsystemsBuilder() {
        return new SubsystemsMigration.Builder<>();
    }

    public StandaloneServerConfigurationMigration.Builder<SC> standaloneMigrationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new StandaloneServerConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }

    public DomainConfigurationMigration.Builder<SC> domainConfigurationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new DomainConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }

    public HostConfigurationMigration.Builder<SC> hostConfigurationBuilder(ServerConfigurationMigration.XMLConfigurationProvider<SC> xmlConfigurationProvider) {
        return new HostConfigurationMigration.Builder<>(xmlConfigurationProvider);
    }

    public HostMigration.Builder<SC> hostBuilder() {
        return new HostMigration.Builder<>();
    }

}
