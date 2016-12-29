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

import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.wfly10.config.task.DomainConfigurationMigration;
import org.jboss.migration.wfly10.config.task.DomainMigration;
import org.jboss.migration.wfly10.config.task.HostConfigurationMigration;

/**
 * @author emmartins
 */
public class DomainUpdate<S extends JBossServer<S>> extends DomainMigration<S> {
    public DomainUpdate(Builder<S> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> extends DomainMigration.Builder<S> {

        public Builder<S> domainConfigurations(DomainConfigurationsUpdate<S> domainConfigurationsUpdate) {
            super.domainConfigurations(domainConfigurationsUpdate);
            return this;
        }

        public Builder<S> domainConfigurations(DomainConfigurationMigration<ServerPath<S>> domainConfigurationUpdate) {
            return domainConfigurations(new DomainConfigurationsUpdate<>(domainConfigurationUpdate));
        }

        public Builder<S> domainConfigurations(DomainConfigurationMigration.Builder<ServerPath<S>> domainConfigurationUpdatebuilder) {
            return domainConfigurations(domainConfigurationUpdatebuilder.build());
        }

        public Builder<S> hostConfigurations(HostConfigurationsUpdate<S> hostConfigurationsUpdate) {
            super.hostConfigurations(hostConfigurationsUpdate);
            return this;
        }

        public Builder<S> hostConfigurations(HostConfigurationMigration<ServerPath<S>> hostConfigurationUpdate) {
            return hostConfigurations(new HostConfigurationsUpdate<>(hostConfigurationUpdate));
        }

        public Builder<S> hostConfigurations(HostConfigurationMigration.Builder<ServerPath<S>> hostConfigurationUpdateBuilder) {
            return hostConfigurations(hostConfigurationUpdateBuilder.build());
        }

        public DomainUpdate<S> build() {
            return new DomainUpdate<>(this);
        }
    }
}
