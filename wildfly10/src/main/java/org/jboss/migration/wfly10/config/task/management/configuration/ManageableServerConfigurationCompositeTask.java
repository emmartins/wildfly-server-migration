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

package org.jboss.migration.wfly10.config.task.management.configuration;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component2.BuildParameters;
import org.jboss.migration.core.task.component2.CompositeTask;
import org.jboss.migration.core.task.component2.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesLeafTask;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationCompositeTask extends CompositeTask {

    protected ManageableServerConfigurationCompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    public static class Builder<S> extends BaseBuilder<ManageableServerConfigurationParameters<S>, Builder<S>> {

        public Builder() {
        }

        protected Builder(Builder<S> other) {
            super(other);
        }

        @Override
        public Builder<S> clone() {
            return new Builder<>(this);
        }

        @Override
        protected Builder<S> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new ManageableServerConfigurationCompositeTask(name, taskRunnable);
        }

        // TODO extract common code from resource(s) composite tasks' builders

        public <R extends ManageableResource> Builder<S> subtask(Class<R> resourceType, ManageableResourcesCompositeTask.Builder<S, R> builder) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(ManageableResourceSelector<R> resourceSelector, ManageableResourcesCompositeTask.Builder<S, R> builder) {
            return subtask(getResourcesMapper(resourceSelector), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(Class<R> resourceType, ManageableResourcesLeafTask.Builder<S, R> builder) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(ManageableResourceSelector<R> resourceSelector, ManageableResourcesLeafTask.Builder<S, R> builder) {
            return subtask(getResourcesMapper(resourceSelector), builder);
        }

        protected <R extends ManageableResource> BuildParameters.Mapper<ManageableServerConfigurationParameters<S>, ManageableResourcesBuildParameters<S, R>> getResourcesMapper(ManageableResourceSelector<R> resourceSelector) {
            return sourceParams -> {
                final Collection<? extends R> resources = resourceSelector.fromResources(sourceParams.getServerConfiguration());
                return Collections.singleton(new ManageableResourcesBuildParameters<S, R>() {
                    @Override
                    public Collection<? extends R> getResources() {
                        return resources;
                    }

                    @Override
                    public S getSource() {
                        return sourceParams.getSource();
                    }

                    @Override
                    public ManageableServerConfiguration getServerConfiguration() {
                        return sourceParams.getServerConfiguration();
                    }
                });
            };
        }

        public <R extends ManageableResource> Builder<S> subtask(Class<R> resourceType, ManageableResourceCompositeTask.Builder<S, R> builder) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(ManageableResourceSelector<R> resourceSelector, ManageableResourceCompositeTask.Builder<S, R> builder) {
            return subtask(getResourceMapper(resourceSelector), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(Class<R> resourceType, ManageableResourceLeafTask.Builder<S, R> builder) {
            return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        public <R extends ManageableResource> Builder<S> subtask(ManageableResourceSelector<R> resourceSelector, ManageableResourceLeafTask.Builder<S, R> builder) {
            return subtask(getResourceMapper(resourceSelector), builder);
        }

        protected <R extends ManageableResource> BuildParameters.Mapper<ManageableServerConfigurationParameters<S>, ManageableResourceBuildParameters<S, R>> getResourceMapper(ManageableResourceSelector<R> resourceSelector) {
            return sourceParams -> {
                final Collection<? extends R> resources = resourceSelector.fromResources(sourceParams.getServerConfiguration());
                return resources.stream().map(resource -> new ManageableResourceBuildParameters<S, R>() {
                    @Override
                    public R getResource() {
                        return resource;
                    }

                    @Override
                    public S getSource() {
                        return sourceParams.getSource();
                    }

                    @Override
                    public ManageableServerConfiguration getServerConfiguration() {
                        return sourceParams.getServerConfiguration();
                    }
                }).collect(toSet());
            };
        }
    }
}
