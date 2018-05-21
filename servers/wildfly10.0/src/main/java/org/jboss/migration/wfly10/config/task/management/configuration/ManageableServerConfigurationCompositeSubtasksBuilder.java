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

import org.jboss.migration.core.task.component.CompositeSubtasksBuilder;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableServerConfigurationToResourceParametersMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableServerConfigurationToResourcesParametersMapper;

/**
 * @author emmartins
 */
public interface ManageableServerConfigurationCompositeSubtasksBuilder<S, T extends ManageableServerConfigurationCompositeSubtasksBuilder<S, T>> extends CompositeSubtasksBuilder<ManageableServerConfigurationBuildParameters<S>, T> {

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableServerConfigurationToResourcesParametersMapper<>(resourceSelector), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableServerConfigurationToResourcesParametersMapper<>(resourceSelector), builder);
    }

    // --

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableServerConfigurationToResourceParametersMapper<>(resourceSelector), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableServerConfigurationToResourceParametersMapper<>(resourceSelector), builder);
    }
}
