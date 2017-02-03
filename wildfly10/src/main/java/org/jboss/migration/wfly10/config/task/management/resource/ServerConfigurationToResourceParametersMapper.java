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
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationBuildParameters;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public class ServerConfigurationToResourceParametersMapper<S, T extends ManageableResource, R extends ManageableResource> implements BuildParameters.Mapper<ServerConfigurationBuildParameters<S>, ResourceBuildParameters<S, R>> {

    private final ManageableResourceSelector<R> resourceSelector;

    public ServerConfigurationToResourceParametersMapper(ManageableResourceSelector<R> resourceSelector) {
        this.resourceSelector = resourceSelector;
    }

    public ServerConfigurationToResourceParametersMapper(Class<R> resourceType) {
        this(ManageableResourceSelectors.selectResources(resourceType));
    }

    public ServerConfigurationToResourceParametersMapper(Class<R> resourceType, String resourceName) {
        this(ManageableResourceSelectors.selectResources(resourceType, resourceName));
    }

    @Override
    public Collection<ResourceBuildParameters<S, R>> apply(ServerConfigurationBuildParameters<S> params) {
        return resourceSelector.fromResources(params.getServerConfiguration()).stream().map(resource -> new ResourceBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), resource)).collect(toSet());
    }
}
