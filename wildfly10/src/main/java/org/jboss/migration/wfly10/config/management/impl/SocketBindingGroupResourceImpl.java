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
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author emmartins
 */
public class SocketBindingGroupResourceImpl extends ManageableResourceImpl implements SocketBindingGroupResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(SocketBindingGroupResource.class, SocketBindingResourceImpl.TYPE);

    public static class Factory extends ManageableResourceImpl.Factory<SocketBindingGroupResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, SERVER_GROUP, parentResource, serverConfiguration);
        }
        @Override
        public SocketBindingGroupResource newResourceInstance(String resourceName) {
            return new SocketBindingGroupResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }

    private final SocketBindingResourceImpl.Factory socketBindingResources;

    private SocketBindingGroupResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
        socketBindingResources = new SocketBindingResourceImpl.Factory(pathAddress, this, serverConfiguration);
        addChildResourceFactory(socketBindingResources);
    }

    @Override
    public SocketBindingResource getSocketBindingResource(String resourceName) throws IOException {
        return socketBindingResources.getResource(resourceName);
    }

    @Override
    public List<SocketBindingResource> getSocketBindingResources() throws IOException {
        return socketBindingResources.getResources();
    }

    @Override
    public Set<String> getSocketBindingResourceNames() throws IOException {
        return socketBindingResources.getResourceNames();
    }

    @Override
    public PathAddress getSocketBindingResourcePathAddress(String resourceName) {
        return socketBindingResources.getResourcePathAddress(resourceName);
    }

    @Override
    public void removeSocketBindingResource(String resourceName) throws IOException {
        socketBindingResources.removeResource(resourceName);
    }
}