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

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostControllerConfiguration;
import org.jboss.migration.wfly10.config.task.factory.HostsManagementTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;

/**
 * Host config migration.
 * @author emmartins
 */
public class HostConfigurationMigration<S> extends ServerConfigurationMigration<S, HostControllerConfiguration> {

    public static final String HOST = "host";

    protected HostConfigurationMigration(Builder builder) {
        super(builder.builder);
    }

    public static class Builder<S> {

        private final ServerConfigurationMigration.Builder<S, HostControllerConfiguration> builder;

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            builder = new ServerConfigurationMigration.Builder<>(HOST, xmlConfigurationProvider);
            builder.manageableConfigurationProvider(new EmbeddedHostControllerConfiguration.HostConfigFileMigrationFactory());
        }

        public HostConfigurationMigration<S> build() {
            return new HostConfigurationMigration<>(this);
        }

        public Builder<S> subtask(final XMLConfigurationSubtaskFactory<S> taskFactory) {
            builder.subtask(taskFactory);
            return this;
        }

        public Builder<S> subtask(final HostsManagementTaskFactory<S> taskFactory) {
            builder.subtask(new ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) {
                    return taskFactory.getTask(source, configuration);
                }
            });
            return this;
        }

        public Builder<S> subtask(final HostMigration.Builder<S> hostMigrationBuilder) {
            return subtask(hostMigrationBuilder.build());
        }
    }
}
