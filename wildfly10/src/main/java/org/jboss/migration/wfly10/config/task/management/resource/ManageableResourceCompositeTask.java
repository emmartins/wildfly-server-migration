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
import org.jboss.migration.core.task.component.CompositeTask;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourceToResourcesParametersDirectMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourceToResourcesParametersMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesCompositeSubtasksBuilder;

/**
 * @author emmartins
 */
public class ManageableResourceCompositeTask extends CompositeTask {

    protected ManageableResourceCompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    public abstract static class BaseBuilder<S, R extends ManageableResource, T extends BaseBuilder<S, R, T>> extends CompositeTask.BaseBuilder<ManageableResourceBuildParameters<S, R>, T> implements ManageableResourceCompositeTaskBuilder<S, R, T> {
        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new ManageableResourceCompositeTask(name, taskRunnable);
        }

        protected <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, ManageableResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        protected <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, String resourceName, ManageableResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
        }

        protected <R1 extends ManageableResource> T subtasks(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(new ManageableResourceToResourcesParametersMapper<>(resourceSelector), builder);
        }

        // --

        protected <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, ManageableResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(ManageableResourceSelectors.selectResources(resourceType), builder);
        }

        protected <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, String resourceName, ManageableResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
        }

        protected <R1 extends ManageableResource> T subtasks(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
            return subtasks(new ManageableResourceToResourceParametersMapper<>(resourceSelector), builder);
        }

        // --
        protected T subtasks(ManageableResourcesCompositeSubtasksBuilder<S, R, ?> builder) {
            return subtasks(new ManageableResourceToResourcesParametersDirectMapper<>(), builder);
        }
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<S, R, Builder<S, R>> {
        @Override
        protected Builder<S, R> getThis() {
            return this;
        }
    }
}