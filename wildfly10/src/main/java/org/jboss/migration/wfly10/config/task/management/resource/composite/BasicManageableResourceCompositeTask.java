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

package org.jboss.migration.wfly10.config.task.management.resource.composite;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.NameFactory;
import org.jboss.migration.core.task.composite.CompositeTask;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.management.resource.component.ManageableResourceComponentTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public class BasicManageableResourceCompositeTask<S, R extends ManageableResource> extends ManageableResourceCompositeTask<S, R, BasicManageableResourceCompositeTask> {

    private final S source;
    private final Collection<? extends R> resources;

    private BasicManageableResourceCompositeTask(Builder<S, R> builder) {
        super(builder);
        this.source = builder.source;
        this.resources = builder.resources;
    }

    public S getSource() {
        return source;
    }

    public Collection<? extends R> getResources() {
        return resources;
    }

    // extensible component interfaces

    protected interface SubtaskExecutor<S, R extends ManageableResource> extends ManageableResourceCompositeTask.SubtaskExecutor<S, R, BasicManageableResourceCompositeTask<S, R>>  {
    }

    protected interface SubtaskFactory<S, R extends ManageableResource> extends ManageableResourceCompositeTask.SubtaskFactory<S, R, BasicManageableResourceCompositeTask<S, R>>  {
    }

    protected interface RunnableExt<S, R extends ManageableResource, T extends BasicManageableResourceCompositeTask<S, R>> extends CompositeTask.Runnable<T> {
    }

    // default non extensible component interfaces

    public interface SubtaskExecutor<S, R extends ManageableResource> extends SubtaskExecutorExt<S, R, BasicManageableResourceCompositeTask<S, R>> {
        void run(Collection<? extends R> resources, T parentTask, TaskContext parentTaskContext) throws Exception;
    }

    public interface SubtaskFactory<S, R extends ManageableResource> extends SubtaskFactoryExt<S, R, BasicManageableResourceCompositeTask<S, R>> {
        ServerMigrationTask getTask(R resource, T parentTask, TaskContext parentTaskContext) throws Exception;
    }

    protected interface Runnable<S, R extends ManageableResource> extends RunnableExt<S, R, BasicManageableResourceCompositeTask<S, R>> {
    }

    public static class Builder<S, R extends ManageableResource> extends ManageableResourceCompositeTask.Builder<S, R, BasicManageableResourceCompositeTask<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName name, Class<R> resourceType) {
            super(name, resourceType);
        }

        public Builder(NameFactory<BasicManageableResourceCompositeTask<S, R>> nameFactory, Class<R> resourceType) {
            super(nameFactory, resourceType);
        }

        public Builder(ServerMigrationTaskName name, ManageableResourceSelector<R> selector) {
            super(name, selector);
        }

        public Builder(NameFactory<BasicManageableResourceCompositeTask<S, R>> nameFactory, ManageableResourceSelector<R> selector) {
            super(nameFactory, selector);
        }

        protected Builder(Builder other) {
            super(other);
        }

        @Override
        protected Builder<S, R> getThis() {
            return this;
        }

        @Override
        public Builder clone() {
            return new Builder(this);
        }

        @Override
        public BasicManageableResourceCompositeTask<S, R> build() {
            return new BasicManageableResourceCompositeTask<>(this);
        }
    }
}
