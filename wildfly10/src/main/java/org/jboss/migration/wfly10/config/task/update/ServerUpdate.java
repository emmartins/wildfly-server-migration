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
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.DomainConfigurationMigration;
import org.jboss.migration.wfly10.config.task.DomainMigration;
import org.jboss.migration.wfly10.config.task.HostConfigurationMigration;
import org.jboss.migration.wfly10.config.task.MigrationBuilders;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.jboss.migration.wfly10.config.task.ServerMigration;
import org.jboss.migration.wfly10.config.task.StandaloneServerConfigurationMigration;
import org.jboss.migration.wfly10.config.task.StandaloneServerMigration;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

/**
 * @author emmartins
 */
public class ServerUpdate<S extends JBossServer<S>> extends ServerMigration<S> {

    public ServerUpdate(ServerMigration.Builder<S> builder) {
        super(builder);
    }

    public static class Builder<S extends JBossServer<S>> extends ServerMigration.Builder<S> {

        @Override
        public Builder<S> subtask(SubtaskFactory<S> subtaskFactory) {
            super.subtask(subtaskFactory);
            return this;
        }

        public Builder<S> domain(DomainMigration<S> domainUpdate) {
            return subtask(domainUpdate);
        }

        public Builder<S> domain(DomainMigration.Builder<S> domainUpdateBuilder) {
            return domain(domainUpdateBuilder.build());
        }

        public Builder<S> standaloneServer(StandaloneServerMigration<S> standaloneServerUpdate) {
            return subtask(standaloneServerUpdate);
        }

        public Builder<S> standaloneServer(StandaloneServerConfigurationsUpdate<S> configurationsMigration) {
            return standaloneServer(new StandaloneServerMigration(configurationsMigration));
        }

        public Builder<S> standaloneServer(StandaloneServerConfigurationMigration<ServerPath<S>> standaloneServerConfigurationUpdate) {
           return standaloneServer(new StandaloneServerConfigurationsUpdate(standaloneServerConfigurationUpdate));
        }

        public Builder<S> standaloneServer(StandaloneServerConfigurationMigration.Builder<ServerPath<S>> standaloneServerConfigurationUpdateBuilder) {
            return standaloneServer(standaloneServerConfigurationUpdateBuilder.build());
        }

        public ServerUpdate<S> build() {
            return new ServerUpdate(this);
        }
    }

    public static class Builders<S extends JBossServer<S>> extends MigrationBuilders<S, ServerPath<S>> {

        private final ServerConfigurationMigration.XMLConfigurationProvider<S> defaultXmlConfigurationProvider = new CopySourceXMLConfiguration();

        public ServerUpdate.Builder<S> serverUpdateBuilder() {
            return new ServerUpdate.Builder();
        }

        public DomainConfigurationMigration.Builder<ServerPath<S>> domainConfigurationBuilder() {
            return new DomainConfigurationMigration.Builder(defaultXmlConfigurationProvider);
        }

        public HostConfigurationMigration.Builder<ServerPath<S>> hostConfigurationBuilder() {
            return new HostConfigurationMigration.Builder(defaultXmlConfigurationProvider);
        }

        public StandaloneServerConfigurationMigration.Builder<ServerPath<S>> standaloneConfigurationBuilder() {
            return new StandaloneServerConfigurationMigration.Builder(defaultXmlConfigurationProvider);
        }

        public DomainUpdate.Builder<S> domainBuilder() {
            return new DomainUpdate.Builder<>();
        }

        public HostUpdate.Builder<S> hostBuilder() {
            return new HostUpdate.Builder();
        }
    }

    public static class SimpleBuilder<S extends JBossServer<S>> {

        private final Builders<S> builders = new Builders<>();
        private StandaloneServerConfigurationMigration.Builder<ServerPath<S>> standaloneBuilder;
        private DomainConfigurationMigration.Builder<ServerPath<S>> domainBuilder;
        private HostUpdate.Builder<S> hostBuilder;

        public synchronized StandaloneServerConfigurationMigration.Builder<ServerPath<S>> standaloneConfigBuilder() {
            if (standaloneBuilder == null) {
                standaloneBuilder = builders.standaloneConfigurationBuilder();
            }
            return standaloneBuilder;
        }

        public synchronized DomainConfigurationMigration.Builder<ServerPath<S>> domainConfigBuilder() {
            if (domainBuilder == null) {
                domainBuilder = builders.domainConfigurationBuilder();
            }
            return domainBuilder;
        }

        public synchronized HostUpdate.Builder<S> hostConfigBuilder() {
            if (hostBuilder == null) {
                hostBuilder = builders.hostBuilder();
            }
            return hostBuilder;
        }

        public SimpleBuilder<S> standaloneConfigTask(ServerConfigurationMigration.XMLConfigurationSubtaskFactory<ServerPath<S>> subtaskFactory) {
            standaloneConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> standaloneConfigTask(ManageableServerConfigurationTaskFactory<ServerPath<S>, StandaloneServerConfiguration> subtaskFactory) {
            standaloneConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> standaloneConfigTask(StandaloneServerConfigurationTaskFactory<ServerPath<S>> subtaskFactory) {
            standaloneConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> domainConfigTask(ServerConfigurationMigration.XMLConfigurationSubtaskFactory<ServerPath<S>> subtaskFactory) {
            domainConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> domainConfigTask(ManageableServerConfigurationTaskFactory<ServerPath<S>, HostControllerConfiguration> subtaskFactory) {
            domainConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> domainConfigTask(DomainConfigurationTaskFactory<ServerPath<S>> subtaskFactory) {
            domainConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> hostConfigTask(ManageableServerConfigurationTaskFactory<ServerPath<S>, HostConfiguration> subtaskFactory) {
            hostConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public SimpleBuilder<S> hostConfigTask(HostConfigurationTaskFactory<ServerPath<S>> subtaskFactory) {
            hostConfigBuilder().subtask(subtaskFactory);
            return this;
        }

        public ServerUpdate<S> build() {
            final Builder<S> builder = new Builder<>();
            if (standaloneBuilder != null) {
                builder.standaloneServer(standaloneBuilder);
            }
            if (domainBuilder != null || hostBuilder != null) {
                final DomainUpdate.Builder<S> domain = builders.domainBuilder();
                if (domainBuilder != null) {
                    domain.domainConfigurations(domainBuilder);
                }
                if (hostBuilder != null) {
                    domain.hostConfigurations(builders.hostConfigurationBuilder().subtask(hostBuilder));
                }
                builder.domain(domain);
            }
            return builder.build();
        }
    }
}
