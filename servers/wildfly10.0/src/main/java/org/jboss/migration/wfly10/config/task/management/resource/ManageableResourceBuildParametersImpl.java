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

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationBuildParametersImpl;

/**
 * @author emmartins
 */
public class ManageableResourceBuildParametersImpl<S, R extends ManageableResource> extends ManageableServerConfigurationBuildParametersImpl<S> implements ManageableResourceBuildParameters<S, R> {

    private final R resource;

    public ManageableResourceBuildParametersImpl(S source, R resource) {
        super(source, resource.getServerConfiguration());
        this.resource = resource;
    }

    @Override
    public R getResource() {
        return resource;
    }
}
