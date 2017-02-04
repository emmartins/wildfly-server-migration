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

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.CompositeSubtasksBuilder;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ServerConfigurationToResourceParametersMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ServerConfigurationToResourcesParametersMapper;

/**
 * @author emmartins
 */
public interface ServerConfigurationCompositeSubtasksBuilder<S, T extends ServerConfigurationCompositeSubtasksBuilder<S, T>> extends CompositeSubtasksBuilder<ServerConfigurationBuildParameters<S>, T> {

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ServerConfigurationToResourcesParametersMapper<>(resourceSelector), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ServerConfigurationToResourcesParametersMapper<>(resourceSelector), builder);
    }

    // --

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ServerConfigurationToResourceParametersMapper<>(resourceSelector), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ServerConfigurationToResourceParametersMapper<>(resourceSelector), builder);
    }

    @Override
    ServerConfigurationCompositeSubtasks build(ServerConfigurationBuildParameters<S> params, ServerMigrationTaskName taskName);
}
