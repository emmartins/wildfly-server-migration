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

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class ManageableResourceImpl implements ManageableResource {

    private final String resourceName;
    private final PathAddress pathAddress;
    private final ManageableServerConfiguration serverConfiguration;

    public ManageableResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        this.resourceName = resourceName;
        this.pathAddress = pathAddress;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public PathAddress getResourcePathAddress() {
        return pathAddress;
    }

    @Override
    public Class<ManageableResource> getResourceType() {
        return ManageableResource.class;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}
