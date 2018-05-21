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

import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.CompositeSubtasksBuilder;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesToResourceParametersMapper;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public interface ManageableResourcesCompositeSubtasksBuilder<S, R extends ManageableResource, T extends ManageableResourcesCompositeSubtasksBuilder<S, R, T>> extends CompositeSubtasksBuilder<ManageableResourcesBuildParameters<S, R>, T>, ManageableResourcesTaskRunnableBuilder<S, R> {

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, String resourceName, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourcesComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableResourcesToResourcesParametersMapper<>(resourceSelector), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, String resourceName, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourcesTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableResourcesToResourcesParametersMapper<>(resourceSelector), builder);
    }

    // --

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, String resourceName, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourceComponentTaskBuilder<S, R1, ?> builder) {
        return subtask(new ManageableResourcesToResourceParametersMapper<>(resourceSelector), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default  <R1 extends ManageableResource> T subtask(Class<R1> resourceType, String resourceName, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default  <R1 extends ManageableResource> T subtask(ManageableResourceSelector<R1> resourceSelector, ManageableResourceTaskRunnableBuilder<S, R1> builder) {
        return subtask(new ManageableResourcesToResourceParametersMapper<>(resourceSelector), builder);
    }

    // ---

    default T subtask(ManageableResourcesTaskRunnableBuilder<S, ? super R> builder) {
        final TaskRunnable.Builder<ManageableResourcesBuildParameters<S, R>> rBuilder = params -> builder.build(new ManageableResourcesBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), params.getResources()));
        return subtask(rBuilder);
    }

    /*
    default T subtask(ManageableResourcesComponentTaskBuilder<S, ? super R, ?> builder) {
        final TaskRunnable.Builder<ManageableResourcesBuildParameters<S, R>> rBuilder = params -> context -> context.execute(builder.build(new ManageableResourcesBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), params.getResources()))).getResult();
        return subtask(rBuilder);
    }*/

    default T subtask(ManageableResourceComponentTaskBuilder<S, ? super R, ?> builder) {
        final TaskRunnable.Builder<ManageableResourcesBuildParameters<S, R>> rBuilder = params -> {
            Collection<ManageableResourceBuildParameters<S, R>> rParams =  params.getResources().stream().map(resource -> new ManageableResourceBuildParametersImpl<>(params.getSource(), (R)resource)).collect(toSet());
            return context -> {
                ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for(ManageableResourceBuildParameters<S, R> rParam : rParams) {
                    if (context.execute(builder.build(new ManageableResourceBuildParametersImpl<>(rParam.getSource(), rParam.getResource()))).getResult().getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            };
        };
        return subtask(rBuilder);
    }

    default T subtask(ManageableResourceTaskRunnableBuilder<S, ? super R> builder) {
        final TaskRunnable.Builder<ManageableResourcesBuildParameters<S, R>> rBuilder = params -> {
            Collection<ManageableResourceBuildParameters<S, R>> rParams =  params.getResources().stream().map(resource -> new ManageableResourceBuildParametersImpl<>(params.getSource(), (R)resource)).collect(toSet());
            return context -> {
                ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for(ManageableResourceBuildParameters<S, R> rParam : rParams) {
                    if (builder.build(new ManageableResourceBuildParametersImpl<>(rParam.getSource(), rParam.getResource())).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            };
        };
        return subtask(rBuilder);
    }
}
