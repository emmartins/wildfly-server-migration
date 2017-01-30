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
import org.jboss.migration.core.task.component2.CompositeTask;
import org.jboss.migration.core.task.component2.LeafTask;
import org.jboss.migration.core.task.component2.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import java.util.Collections;

/**
 * @author emmartins
 */
public interface ManageableServerConfigurationTasks {

    interface Parameters<S> extends BuildParameters {
        S getSource();
        ManageableServerConfiguration getServerConfiguration();
    }

    interface Mappers {
        static <S, T extends ManageableResource, R extends ManageableServerConfiguration> org.jboss.migration.core.task.component2.BuildParameters.Mapper<ManageableResourceTasks.ResourceBuildParameters<S, T>, Parameters<S>> from(ManageableResourceTasks.ResourcesBuildParameters<S, T> tParameters) {
            return resourcesBuildParameters -> {
                final S rSource = resourcesBuildParameters.getSource();
                final ManageableServerConfiguration rServerConfiguration = resourcesBuildParameters.getServerConfiguration();
                return Collections.singleton(new Parameters<S>() {
                    @Override
                    public S getSource() {
                        return rSource;
                    }
                    @Override
                    public ManageableServerConfiguration getServerConfiguration() {
                        return rServerConfiguration;
                    }
                });
            };
        }
        static <S, T extends ManageableResource, R extends ManageableServerConfiguration> org.jboss.migration.core.task.component2.BuildParameters.Mapper<ManageableResourceTasks.ResourceBuildParameters<S, T>, Parameters<S>> from(ManageableResourceTasks.ResourceBuildParameters<S, T> tParameters) {
            return resourceBuildParameters -> {
                final S rSource = resourceBuildParameters.getSource();
                final ManageableServerConfiguration rServerConfiguration = resourceBuildParameters.getServerConfiguration();
                return Collections.singleton(new Parameters<S>() {
                    @Override
                    public S getSource() {
                        return rSource;
                    }
                    @Override
                    public ManageableServerConfiguration getServerConfiguration() {
                        return rServerConfiguration;
                    }
                });
            };
        }
    }

    class Leaf extends LeafTask {

        protected Leaf(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            super(name, taskRunnable);
        }

        public static class Builder<S> extends AbstractBuilder<Parameters<S>, Builder<S>> {

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
                return new Leaf(name, taskRunnable);
            }
        }
    }

    class Composite extends CompositeTask {

        protected Composite(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            super(name, taskRunnable);
        }

        public static class Builder<S> extends AbstractBuilder<Parameters<S>, Builder<S>> {

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
                return new Composite(name, taskRunnable);
            }
        }
    }
}
