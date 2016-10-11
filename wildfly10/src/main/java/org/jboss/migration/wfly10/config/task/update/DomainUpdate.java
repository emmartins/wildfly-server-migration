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
import org.jboss.migration.wfly10.config.task.DomainMigration;

/**
 * @author emmartins
 */
public class DomainUpdate<S extends JBossServer<S>> extends DomainMigration<S> {
    public DomainUpdate(Builder<S> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> extends DomainMigration.Builder<S> {

        public Builder<S> domainConfigurationsMigration(DomainConfigurationsUpdate<S> domainConfigurationsUpdate) {
            super.domainConfigurations(domainConfigurationsUpdate);
            return this;
        }

        public Builder<S> domainConfigurationsMigration(DomainConfigurationUpdate<S> domainConfigurationUpdate) {
            return domainConfigurationsMigration(new DomainConfigurationsUpdate<>(domainConfigurationUpdate));
        }

        public Builder<S> domainConfigurationsMigration(DomainConfigurationUpdate.Builder<S> domainConfigurationUpdatebuilder) {
            return domainConfigurationsMigration(domainConfigurationUpdatebuilder.build());
        }

        public Builder<S> hostConfigurationsMigration(HostConfigurationsUpdate<S> hostConfigurationsUpdate) {
            super.hostConfigurations(hostConfigurationsUpdate);
            return this;
        }

        public Builder<S> hostConfigurationsMigration(HostConfigurationUpdate<S> hostConfigurationUpdate) {
            return hostConfigurationsMigration(new HostConfigurationsUpdate<>(hostConfigurationUpdate));
        }

        public Builder<S> hostConfigurationsMigration(HostConfigurationUpdate.Builder<S> hostConfigurationUpdateBuilder) {
            return hostConfigurationsMigration(hostConfigurationUpdateBuilder.build());
        }

        public Builder<S> defaultHostConfigurationsMigration() {
            return hostConfigurationsMigration(new HostConfigurationUpdate.Builder<S>().hostUpdate(new HostUpdate.Builder<S>().build()));
        }

        public DomainUpdate<S> build() {
            return new DomainUpdate<>(this);
        }
    }
}
