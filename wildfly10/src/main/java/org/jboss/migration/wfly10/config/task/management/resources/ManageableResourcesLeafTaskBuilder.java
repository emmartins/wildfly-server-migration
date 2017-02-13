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

package org.jboss.migration.wfly10.config.task.management.resources;

import org.jboss.migration.core.task.component.LeafTaskBuilder;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesToResourceParametersDirectMapper;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesToResourceParametersMapper;

/**
 * @author emmartins
 */
public interface ManageableResourcesLeafTaskBuilder<S, R extends ManageableResource, T extends ManageableResourcesLeafTaskBuilder<S, R, T>> extends LeafTaskBuilder<ManageableResourcesBuildParameters<S, R>, T>, ManageableResourcesComponentTaskBuilder<S, R, T> {

    default <R1 extends ManageableResource> T runBuilder(Class<? extends R1> resourceType, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T runBuilder(Class<? extends R1> resourceType, String resourceName, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T runBuilder(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(new ManageableResourcesToResourceParametersMapper<>(resourceSelector), builder);
    }

    // --

    default <R1 extends ManageableResource> T runBuilder(Class<? extends R1> resourceType, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T runBuilder(Class<? extends R1> resourceType, String resourceName, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T runBuilder(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return runBuilder(new ManageableResourcesToResourcesParametersMapper<>(resourceSelector), builder);
    }

    default T resourceRunBuilder(ManageableResourceTaskRunnableBuilder<S, R> builder) {
        return runBuilder(new ManageableResourcesToResourceParametersDirectMapper<>(), builder);
    }
}
