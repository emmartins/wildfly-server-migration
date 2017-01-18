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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class ManageableResourcesImpl implements ManageableResources {

    private final ManageableServerConfiguration serverConfiguration;
    protected final PathAddress parentPathAddress;
    private final String type;

    public ManageableResourcesImpl(String type, PathAddress parentPathAddress, ManageableServerConfiguration serverConfiguration) {
        this.type = type;
        this.parentPathAddress = parentPathAddress;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    protected PathAddress getPathAddress(PathElement... elements) {
        final PathAddress parentAddress = parentPathAddress;
        return parentAddress != null ? parentAddress.append(elements) : pathAddress(elements);
    }

    @Override
    public PathAddress getResourcePathAddress(String resourceName) {
        return getPathAddress(pathElement(type, resourceName));
    }

    @Override
    public Set<String> getResourceNames() throws IOException {
        try {
            final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, parentPathAddress);
            op.get(CHILD_TYPE).set(type);
            final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
            Set<String> result = new HashSet<>();
            for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                result.add(resultNode.asString());
            }
            return result;
        } catch (ManagementOperationException e) {
            try {
                final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_TYPES_OPERATION, parentPathAddress);
                final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
                boolean childrenTypeFound = false;
                for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                    if (type.equals(resultNode.asString())) {
                        childrenTypeFound = true;
                        break;
                    }
                }
                if (!childrenTypeFound) {
                    return Collections.emptySet();
                }
            } catch (Throwable t) {
                // ignore
            }
            throw e;
        }
    }

    @Override
    public ModelNode getResourceConfiguration(String name) throws IOException {
        if (!getResourceNames().contains(name)) {
            return null;
        }
        final PathAddress address = getResourcePathAddress(name);
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = serverConfiguration.executeManagementOperation(op);
        return result.get(RESULT);
    }

    @Override
    public void removeResource(String resourceName) throws IOException {
        final PathAddress address = getResourcePathAddress(resourceName);
        final ModelNode op = Util.createRemoveOperation(address);
        serverConfiguration.executeManagementOperation(op);
    }

    @Override
    public <T extends ManageableResources> List<T> findResources(Class<T> resourcesType) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public <T extends ManageableResource> List<T> findResources(Class<T> resourceType, String resourceName) throws IOException {
        return Collections.emptyList();
    }
}