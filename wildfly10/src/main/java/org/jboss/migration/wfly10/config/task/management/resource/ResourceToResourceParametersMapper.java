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

import org.jboss.migration.core.task.component.BuildParameters;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ResourceToResourceParametersMapper<S, T extends ManageableResource, R extends ManageableResource> implements BuildParameters.Mapper<ResourceBuildParameters<S, T>, ResourceBuildParameters<S, R>> {

    private final ManageableResourceSelector<R> resourceSelector;

    public ResourceToResourceParametersMapper(ManageableResourceSelector<R> resourceSelector) {
        this.resourceSelector = resourceSelector;
    }

    public ResourceToResourceParametersMapper(Class<R> resourceType) {
        this(ManageableResourceSelectors.selectResources(resourceType));
    }

    public ResourceToResourceParametersMapper(Class<R> resourceType, String resourceName) {
        this(ManageableResourceSelectors.selectResources(resourceType, resourceName));
    }

    @Override
    public Collection<ResourceBuildParameters<S, R>> apply(ResourceBuildParameters<S, T> tParams) {
        return resourceSelector.fromResources(tParams.getResource()).stream().map(resource -> new ResourceBuildParametersImpl<>(tParams.getSource(), tParams.getServerConfiguration(), resource)).collect(toSet());
    }
}
