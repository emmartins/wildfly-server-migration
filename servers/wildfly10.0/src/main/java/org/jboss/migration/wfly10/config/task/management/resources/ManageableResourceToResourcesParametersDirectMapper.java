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
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;

import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public class ManageableResourceToResourcesParametersDirectMapper<S, R extends ManageableResource> implements BuildParameters.Mapper<ManageableResourceBuildParameters<S, R>, ManageableResourcesBuildParameters<S, R>> {
    @Override
    public Collection<ManageableResourcesBuildParameters<S, R>> apply(ManageableResourceBuildParameters<S, R> params) {
        return Collections.singleton(new ManageableResourcesBuildParametersImpl<>(params.getSource(), params.getServerConfiguration(), Collections.singleton(params.getResource())));
    }
}
