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

import org.jboss.migration.core.task.component.BuildParameters;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceBuildParameters;

import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public class ResourceToResourcesParametersMapper<S, T extends ManageableResource, R extends ManageableResource> implements BuildParameters.Mapper<ResourceBuildParameters<S, T>, ResourcesBuildParameters<S, R>> {

    private final ManageableResourceSelector<R> resourceSelector;

    public ResourceToResourcesParametersMapper(ManageableResourceSelector<R> resourceSelector) {
        this.resourceSelector = resourceSelector;
    }

    public ResourceToResourcesParametersMapper(Class<R> resourceType) {
        this(ManageableResourceSelectors.selectResources(resourceType));
    }

    public ResourceToResourcesParametersMapper(Class<R> resourceType, String resourceName) {
        this(ManageableResourceSelectors.selectResources(resourceType, resourceName));
    }

    @Override
    public Collection<ResourcesBuildParameters<S, R>> apply(ResourceBuildParameters<S, T> tParams) {
        return Collections.singleton(new ResourcesBuildParametersImpl<S, R>(tParams.getSource(), tParams.getServerConfiguration(), resourceSelector.fromResources(tParams.getResource())));
    }
}
