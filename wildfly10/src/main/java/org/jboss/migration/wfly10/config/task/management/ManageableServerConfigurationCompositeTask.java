/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.composite.ManageableResourceCompositeTask;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationCompositeTask<S, R extends ManageableServerConfiguration> extends ManageableResourceCompositeTask<S, R> {

    protected ManageableServerConfigurationCompositeTask(BaseBuilder<S, R, ?, ?> builder) {
        super(builder);
    }

    protected static abstract class BaseBuilder<S, R extends ManageableServerConfiguration, T extends ManageableServerConfigurationCompositeTask<S, R>, B extends BaseBuilder<S, R, T, B>> extends ManageableResourceCompositeTask.BaseBuilder<S, R, T, B> {
        protected BaseBuilder(ServerMigrationTaskName name, Class<R> resourceType) {
            this(task -> name, resourceType);
        }

        protected BaseBuilder(NameFactory<? super T> nameFactory, Class<R> resourceType) {
            super(nameFactory, ManageableResourceSelectors.selectServerConfiguration().andThen(ManageableResourceSelectors.selectResources(resourceType)));
        }

        protected BaseBuilder(BaseBuilder<S, R, T, ?> other) {
            super(other);
        }
    }

    public static class Builder<S, R extends ManageableServerConfiguration> extends BaseBuilder<S, R, ManageableServerConfigurationCompositeTask<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName name, Class<R> serverConfigurationType) {
            super(name, serverConfigurationType);
        }

        public Builder(NameFactory<? super ManageableServerConfigurationCompositeTask> nameFactory, Class<R> serverConfigurationType) {
            super(nameFactory, serverConfigurationType);
        }

        public Builder(Builder<S, R> other) {
            super(other);
        }

        @Override
        public Builder<S, R> clone() {
            return new Builder<>(this);
        }

        @Override
        public ManageableServerConfigurationCompositeTask<S, R> build() {
            return new ManageableServerConfigurationCompositeTask<>(this);
        }
    }
}
