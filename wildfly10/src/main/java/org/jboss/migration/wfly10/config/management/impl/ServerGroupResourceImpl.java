/*
 * Copyright 2016 Red Hat, Inc.
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
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ServerGroupResource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author emmartins
 */
public class ServerGroupResourceImpl extends AbstractManageableResource implements ServerGroupResource {

    public static class Factory extends AbstractManageableResource.Factory<ServerGroupResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SERVER_GROUP, parentResource);
        }
        @Override
        public ServerGroupResource newResourceInstance(String resourceName) {
            return new ServerGroupResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }

    private final JvmResourceImpl.Factory jvmResources;

    private ServerGroupResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
        jvmResources = new JvmResourceImpl.Factory(pathAddress, this);
        addChildResourceFactory(jvmResources);
    }

    @Override
    public JvmResource getJvmResource(String resourceName) throws IOException {
        return jvmResources.getResource(resourceName);
    }

    @Override
    public List<JvmResource> getJvmResources() throws IOException {
        return jvmResources.getResources();
    }

    @Override
    public Set<String> getJvmResourceNames() throws IOException {
        return jvmResources.getResourceNames();
    }

    @Override
    public PathAddress getJvmResourcePathAddress(String resourceName) {
        return jvmResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeJvmResource(String resourceName) throws IOException {
        jvmResources.removeResource(resourceName);
    }
}
