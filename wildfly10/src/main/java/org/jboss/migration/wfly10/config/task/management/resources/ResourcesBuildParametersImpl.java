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

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationBuildParametersImpl;

import java.util.Collection;

/**
 * @author emmartins
 */
public class ResourcesBuildParametersImpl<S, R extends ManageableResource> extends ServerConfigurationBuildParametersImpl<S> implements ResourcesBuildParameters<S, R> {

    private final Collection<? extends R> resources;

    protected ResourcesBuildParametersImpl(S source, ManageableServerConfiguration serverConfiguration, Collection<? extends R> resources) {
        super(source, serverConfiguration);
        this.resources = resources;
    }

    @Override
    public Collection<? extends R> getResources() {
        return resources;
    }
}
