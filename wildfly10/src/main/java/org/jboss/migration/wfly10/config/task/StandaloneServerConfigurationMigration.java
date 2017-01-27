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
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedStandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

/**
 * Standalone config migration.
 * @author emmartins
 */
public class StandaloneServerConfigurationMigration<S> extends ServerConfigurationMigration<S, StandaloneServerConfiguration> {

    protected StandaloneServerConfigurationMigration(ServerConfigurationMigration.Builder<S, StandaloneServerConfiguration> builder) {
        super(builder);
    }

    public static class Builder<S> extends ServerConfigurationMigration.Builder<S, StandaloneServerConfiguration> {

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            super("standalone", xmlConfigurationProvider);
            manageableConfigurationProvider(new EmbeddedStandaloneServerConfiguration.ConfigFileMigrationFactory());
        }

        @Override
        public Builder<S> manageableConfigurationProvider(ManageableConfigurationProvider<StandaloneServerConfiguration> manageableConfigurationProvider) {
            super.manageableConfigurationProvider(manageableConfigurationProvider);
            return this;
        }

        @Override
        public Builder<S> subtask(ManageableServerConfigurationTaskFactory<S, StandaloneServerConfiguration> subtaskFactory) {
            super.subtask(subtaskFactory);
            return this;
        }

        public Builder<S> subtask(final StandaloneServerConfigurationTaskFactory<S> subtaskFactory) {
            subtask(new ManageableServerConfigurationTaskFactory<S, StandaloneServerConfiguration>() {
                @Override
                public ServerMigrationTask getTask(S source, StandaloneServerConfiguration configuration) throws Exception {
                    return subtaskFactory.getTask(source, configuration);
                }
            });
            return this;
        }

        @Override
        public Builder<S> subtask(XMLConfigurationSubtaskFactory<S> subtaskFactory) {
            super.subtask(subtaskFactory);
            return this;
        }

        @Override
        public StandaloneServerConfigurationMigration<S> build() {
            return new StandaloneServerConfigurationMigration<>(this);
        }
    }
}
