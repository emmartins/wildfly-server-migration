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

import org.jboss.migration.core.task.component.CompositeSubtasksBuilder;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourceToResourcesParametersMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesTaskRunnableBuilder;

import java.util.Collections;

/**
 * @author emmartins
 */
public interface ManageableResourceCompositeSubtasksBuilder<S, R extends ManageableResource, T extends ManageableResourceCompositeSubtasksBuilder<S, R, T>> extends CompositeSubtasksBuilder<ManageableResourceBuildParameters<S, R>, T>, ManageableResourceTaskRunnableBuilder<S, R> {

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableResourceToResourcesParametersMapper<>(resourceSelector), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableResourceToResourcesParametersMapper<>(resourceSelector), builder);
    }

    //

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableResourceToResourceParametersMapper<>(resourceSelector), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtask(Class<? extends R1> resourceType, String resourceName, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtask(ManageableResourceSelector<? extends R1> resourceSelector, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableResourceToResourceParametersMapper<>(resourceSelector), builder);
    }

    // ---

    default T subtask(ManageableResourceTaskRunnableBuilder<S, ? super R> builder) {
        final TaskRunnable.Builder<ManageableResourceBuildParameters<S, R>> rBuilder = params -> builder.build(new ManageableResourceBuildParametersImpl<>(params.getSource(), params.getResource()));
        return subtask(rBuilder);
    }
/*
    default T subtask(ManageableResourceComponentTaskBuilder<S, ? super R, ?> builder) {
        final TaskRunnable.Builder<ManageableResourceBuildParameters<S, R>> rBuilder = params -> context -> context.execute(builder.build(new ManageableResourceBuildParametersImpl<>(params.getSource(), params.getResource()))).getResult();
        return subtask(rBuilder);
    }
*/
    default T subtask(ManageableResourcesComponentTaskBuilder<S, ? super R, ?> builder) {
        final TaskRunnable.Builder<ManageableResourceBuildParameters<S, R>> rBuilder = params -> context -> context.execute(builder.build(new ManageableResourcesBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), Collections.singleton(params.getResource())))).getResult();
        return subtask(rBuilder);
    }

    default T subtask(ManageableResourcesTaskRunnableBuilder<S, ? super R> builder) {
        final TaskRunnable.Builder<ManageableResourceBuildParameters<S, R>> rBuilder = params -> builder.build(new ManageableResourcesBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), Collections.singleton(params.getResource())));
        return subtask(rBuilder);
    }
}
