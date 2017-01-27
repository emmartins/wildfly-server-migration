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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.composite.ManageableResourceCompositeTask;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class SubsystemConfigurationParentCompositeTask<S> extends ManageableResourceCompositeTask<S, SubsystemConfiguration.Parent> {

    protected SubsystemConfigurationParentCompositeTask(ManageableResourceCompositeTask.BaseBuilder<S, SubsystemConfiguration.Parent, ?> builder, S source, Collection<? extends ManageableResource> resources) {
        super(builder, source, resources);
    }

    public interface SubtaskExecutor<S> extends ManageableResourceCompositeTask.SubtaskExecutor<S, SubsystemConfiguration.Parent> {
    }

    public interface SubtaskFactory<S> extends ManageableResourceCompositeTask.SubtaskFactory<S, SubsystemConfiguration.Parent> {
    }

    protected static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends ManageableResourceCompositeTask.BaseBuilder<S, SubsystemConfiguration.Parent, B> {
        protected BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName, SubsystemConfiguration.Parent.class);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new SubsystemConfigurationParentCompositeTask<>(this, source, resources);
        }
    }
}