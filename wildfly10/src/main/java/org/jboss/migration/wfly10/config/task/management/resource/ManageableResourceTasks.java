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
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.ServerConfigurationBuildParameters;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public interface ManageableResourceTasks {

    class Leaf extends LeafTask {

        protected Leaf(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            super(name, taskRunnable);
        }

        public static class Builder<S, R extends ManageableResource> extends LeafTask.AbstractBuilder<ManageableResourcesParameters.Single<S, R>, Builder<S, R>> {

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
                return new Leaf(name, taskRunnable);
            }
        }
    }

    class Composite extends CompositeTask {

        protected Composite(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            super(name, taskRunnable);
        }

        public static class Builder<S, R extends ManageableResource> extends CompositeTask.AbstractBuilder<ResourcesBuildParameters<S, R>, Builder<S, R>> {

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
                return new Composite(name, taskRunnable);
            }
        }
    }
}
