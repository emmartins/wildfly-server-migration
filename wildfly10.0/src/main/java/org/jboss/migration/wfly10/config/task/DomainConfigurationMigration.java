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

import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostControllerConfiguration;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;

/**
 * Builder for a domain config migration.
 * @author emmartins
 */
public class DomainConfigurationMigration<S> extends ServerConfigurationMigration<S, HostControllerConfiguration> {

    public static final String DOMAIN = "domain";

    protected DomainConfigurationMigration(Builder<S> builder) {
        super(builder);
    }

    public static class Builder<S> extends ServerConfigurationMigration.BaseBuilder<S, HostControllerConfiguration, Builder<S>> {

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            super(DOMAIN, xmlConfigurationProvider);
            manageableConfigurationProvider(new EmbeddedHostControllerConfiguration.DomainConfigFileMigrationFactory());
        }

        @Override
        public DomainConfigurationMigration<S> build() {
            return new DomainConfigurationMigration<>(this);
        }

        public Builder<S> subtask(final DomainConfigurationTaskFactory<S> subtaskFactory) {
            return subtask((ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration>) (source, configuration) -> subtaskFactory.getTask(source, configuration));
        }

        @Override
        protected Builder<S> getThis() {
            return this;
        }
    }
}
