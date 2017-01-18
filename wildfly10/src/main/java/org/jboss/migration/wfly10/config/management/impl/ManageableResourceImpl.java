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
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

/**
 * @author emmartins
 */
public class ManageableResourceImpl implements ManageableResource {

    private final String resourceName;
    private final PathAddress pathAddress;
    private final ManageableServerConfiguration serverConfiguration;
    protected final Map<Type, ManageableResources> childResources;

    public ManageableResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        this.resourceName = resourceName;
        this.pathAddress = pathAddress;
        this.serverConfiguration = serverConfiguration;
        this.childResources = new HashMap<>();
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
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public <T extends ManageableResource> List<ManageableResources<T>> findChildResources(Class<T> resourceType) throws IOException {
        final List<ManageableResources<T>> result = new ArrayList<>();
        for (Type type : childResources.keySet()) {
            if (resourceType.isAssignableFrom(type.getType())) {
                result.add(childResources.get(type));
            } else {
                final List<Type<?>> childTypes = type.getChildTypes(true);
                for (Type<?> childType : childTypes) {
                    if (resourceType.isAssignableFrom(childType.getType())) {
                        result.addAll(childResources.get(type).findResources(resourceType));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T extends ManageableResource> List<T> findChildResources(Class<T> resourceType, String resourceName) throws IOException {
        final List<T> result = new ArrayList<>();
        for (ManageableResources<T> resources : findChildResources(resourceType)) {
            final T resource = resources.getResource(resourceName);
            if (resource != null) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public ModelNode getResourceConfiguration() throws IOException {
        final PathAddress address = getResourcePathAddress();
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = serverConfiguration.executeManagementOperation(op);
        return result.get(RESULT);
    }
}
