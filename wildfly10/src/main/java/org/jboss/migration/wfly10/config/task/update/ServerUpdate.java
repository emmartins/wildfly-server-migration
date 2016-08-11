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
import org.jboss.migration.wfly10.config.task.MigrationBuilders;
import org.jboss.migration.wfly10.config.task.ServerMigration;

/**
 * @author emmartins
 */
public class ServerUpdate<S extends JBossServer<S>> extends ServerMigration<S> {

    public ServerUpdate(ServerMigration.Builder<S> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> {

        private DomainUpdate<S> domainUpdate;
        private StandaloneServerUpdate<S> standaloneServerUpdate;

        public Builder<S> domainMigration(DomainUpdate<S> domainUpdate) {
            this.domainUpdate = domainUpdate;
            return this;
        }

        public Builder<S> domainMigration(DomainUpdate.Builder<S> domainUpdateBuilder) {
            return domainMigration(domainUpdateBuilder.build());
        }

        public Builder<S> standaloneMigration(StandaloneServerUpdate<S> standaloneServerUpdate) {
            this.standaloneServerUpdate = standaloneServerUpdate;
            return this;
        }

        public Builder<S> standaloneMigration(StandaloneServerConfigurationUpdate<S> standaloneServerConfigurationUpdate) {
            this.standaloneServerUpdate = new StandaloneServerUpdate<>(standaloneServerConfigurationUpdate);
            return this;
        }

        public Builder<S> standaloneMigration(StandaloneServerConfigurationUpdate.Builder<S> standaloneServerConfigurationUpdateBuilder) {
            return standaloneMigration(standaloneServerConfigurationUpdateBuilder.build());
        }

        public ServerUpdate<S> build() {
            final ServerMigration.Builder<S> builder = new ServerMigration.Builder<>();
            if (standaloneServerUpdate != null) {
                builder.addFactory(standaloneServerUpdate);
            }
            if (domainUpdate != null) {
                builder.addFactory(domainUpdate);
            }
            return new ServerUpdate(builder);
        }
    }

    public static class Builders<S extends JBossServer<S>> extends MigrationBuilders<S, ServerPath<S>> {
        public DomainUpdate.Builder<S> domainBuilder() {
            return new DomainUpdate.Builder<>();
        }
        public StandaloneServerConfigurationUpdate.Builder<S> standaloneConfigurationMigrationBuilder() {
            return new StandaloneServerConfigurationUpdate.Builder<>();
        }
        public DomainConfigurationUpdate.Builder<S> domainConfigurationMigrationBuilder() {
            return new DomainConfigurationUpdate.Builder<>();
        }
        public HostConfigurationUpdate.Builder<S> hostConfigurationMigrationBuilder() {
            return new HostConfigurationUpdate.Builder<>();
        }
        public HostUpdate.Builder<S> hostUpdateBuilder() {
            return new HostUpdate.Builder<>();
        }
    }
}
