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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;
import org.jboss.migration.wfly10.config.management.ResourcesManagement;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

/**
 * @author emmartins
 */
public class ResourcesManagementImpl implements ResourcesManagement {

    private final ManageableServerConfiguration configurationManagement;
    private final PathAddress parentPathAddress;
    private final String type;

    public ResourcesManagementImpl(String type, PathAddress parentPathAddress, ManageableServerConfiguration configurationManagement) {
        this.type = type;
        this.parentPathAddress = parentPathAddress;
        this.configurationManagement = configurationManagement;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return configurationManagement;
    }

    @Override
    public PathAddress getParentPathAddress() {
        return parentPathAddress;
    }

    @Override
    public String getResourceType() {
        return type;
    }

    protected PathAddress getPathAddress(PathElement... elements) {
        final PathAddress parentAddress = getParentPathAddress();
        return parentAddress != null ? parentAddress.append(elements) : pathAddress(elements);
    }

    @Override
    public PathAddress getResourcePathAddress(String resourceName) {
        return getPathAddress(pathElement(type, resourceName));
    }

    @Override
    public Set<String> getResourceNames() throws IOException {
        try {
            final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, getParentPathAddress());
            op.get(CHILD_TYPE).set(type);
            final ModelNode opResult = configurationManagement.executeManagementOperation(op);
            Set<String> result = new HashSet<>();
            for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                result.add(resultNode.asString());
            }
            return result;
        } catch (ManagementOperationException e) {
            try {
                final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_TYPES_OPERATION, getParentPathAddress());
                final ModelNode opResult = configurationManagement.executeManagementOperation(op);
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
    public ModelNode getResource(String name) throws IOException {
        if (!getResourceNames().contains(name)) {
            return null;
        }
        final PathAddress address = getResourcePathAddress(name);
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = configurationManagement.executeManagementOperation(op);
        return result.get(RESULT);
    }

    @Override
    public void removeResource(String resourceName) throws IOException {
        final PathAddress address = getResourcePathAddress(resourceName);
        final ModelNode op = Util.createRemoveOperation(address);
        configurationManagement.executeManagementOperation(op);
    }

}