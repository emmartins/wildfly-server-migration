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

package org.jboss.migration.wfly10.config.task.management.resource;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component2.BuildParameters;
import org.jboss.migration.core.task.component2.ComponentTask;
import org.jboss.migration.core.task.component2.CompositeTask;
import org.jboss.migration.core.task.component2.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ManageableResourcesCompositeTask extends CompositeTask {

    protected ManageableResourcesCompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<ManageableResourcesBuildParameters<S, R>, Builder<S, R>> {

        public Builder() {
        }

        protected Builder(Builder<S, R> other) {
            super(other);
        }

        @Override
        public Builder<S, R> clone() {
            return new Builder<>(this);
        }

        @Override
        protected Builder<S, R> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new ManageableResourcesCompositeTask(name, taskRunnable);
        }
    }

    public static class SubtasksBuilder<S, R extends ManageableResource> extends CompositeTask.SubtasksBaseBuilder<ManageableResourcesBuildParameters<S, R>, SubtasksBuilder<S>> {

        @Override
        protected SubtasksBuilder<S, R> getThis() {
            return this;
        }

        // TODO extract common code from server config and resources composite tasks' builders

        public <R1 extends ManageableResource> SubtasksBuilder<S, R> subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourcesCompositeTask.Builder<S, R1> builder) {
            return subtask(getResourcesMapper(resourceSelector), builder);
        }

        public <R1 extends ManageableResource> SubtasksBuilder<S, R> subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourcesLeafTask.Builder<S, R1> builder) {
            return subtask(getResourcesMapper(resourceSelector), builder);
        }

        protected <R1 extends ManageableResource> BuildParameters.Mapper<ManageableResourcesBuildParameters<S, R>, ManageableResourcesBuildParameters<S, R1>> getResourcesMapper(ManageableResourceSelector<R1> resourceSelector) {
            return sourceParams -> {
                final Collection<? extends R1> resources = resourceSelector.fromResources(sourceParams.getResources());
                return Collections.singleton(new ManageableResourcesBuildParameters<S, R1>() {
                    @Override
                    public Collection<? extends R1> getResources() {
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

        public <R1 extends ManageableResource> SubtasksBuilder<S, R> subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourceCompositeTask.Builder<S, R1> builder) {
            return subtask(getResourceMapper(resourceSelector), builder);
        }

        public <R1 extends ManageableResource> SubtasksBuilder<S, R> subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourceLeafTask.Builder<S, R1> builder) {
            return subtask(getResourceMapper(resourceSelector), builder);
        }

        protected <R1 extends ManageableResource> BuildParameters.Mapper<ManageableResourcesBuildParameters<S, R>, ManageableResourceBuildParameters<S, R1>> getResourceMapper(ManageableResourceSelector<R1> resourceSelector) {
            return sourceParams -> {
                final Collection<? extends R1> resources = resourceSelector.fromResources(sourceParams.getResources());
                return resources.stream().map(resource -> new ManageableResourceBuildParameters<S, R1>() {
                    @Override
                    public R1 getResource() {
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

        public SubtasksBuilder<S, R> subtask(ManageableResourceLeafTask.Builder<S, R> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return subtask((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        public SubtasksBuilder<S, R> subtask(ManageableServerConfigurationLeafTask.Builder<S> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return subtask((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        public SubtasksBuilder<S, R> subtask(ManageableServerConfigurationCompositeTask.Builder<S> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return subtask((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

    }
