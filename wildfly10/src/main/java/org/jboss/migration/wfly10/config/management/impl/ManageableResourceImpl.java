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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class ManageableResourceImpl implements ManageableResource {

    private final Map<Class, Factory> childResourceFactories = new HashMap<>();

    private final String resourceName;
    private final PathAddress pathAddress;
    private final ManageableResource parent;
    private final ManageableServerConfiguration serverConfiguration;

    public ManageableResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        this.resourceName = resourceName;
        this.pathAddress = pathAddress;
        this.parent = parent;
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
    public ManageableResource getParent() {
        return parent;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public ModelNode getResourceConfiguration() throws IOException {
        final PathAddress address = getResourcePathAddress();
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = serverConfiguration.executeManagementOperation(op);
        return result.get(RESULT);
    }

    protected void addChildResourceFactory(Factory childResourceFactory) {
        childResourceFactories.put(childResourceFactory.getResourceType().getType(), childResourceFactory);
    }

    protected <T extends ManageableResource> Factory<T> getChildResourceFactory(Class<T> resourceType) {
        return childResourceFactories.get(resourceType);
    }

    @Override
    public <T extends ManageableResource> T getChildResource(Class<T> resourceType, String resourceName) throws IOException {
        final Factory<T> childResourceFactory = getChildResourceFactory(resourceType);
        return childResourceFactory != null ? childResourceFactory.getResource(resourceName) : null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType) throws IOException {
        final Factory<T> childResourceFactory = getChildResourceFactory(resourceType);
        return childResourceFactory != null ? childResourceFactory.getResources() : null;
    }

    @Override
    public Set<Class<? extends ManageableResource>> getChildResourceTypes() {
        return Collections.unmodifiableSet(childResourceFactories.keySet());
    }

    @Override
    public Set<String> getChildResourceNames(Class<? extends ManageableResource> resourceType) throws IOException {
        final Factory childResourceFactory = getChildResourceFactory(resourceType);
        return childResourceFactory != null ? childResourceFactory.getResourceNames() : null;
    }

    @Override
    public <T extends ManageableResource> PathAddress getChildResourcePathAddress(Class<T> resourceType, String resourceName) {
        final Factory<T> childResources = getChildResourceFactory(resourceType);
        return childResources != null ? childResources.getResourcePathAddress(resourceName) : null;
    }

    @Override
    public void removeResource(Class<? extends ManageableResource> resourceType, String resourceName) throws IOException {
        final Factory<?> childResources = getChildResourceFactory(resourceType);
        if (childResources != null) {
            childResources.removeResource(resourceName);
        }
    }

    @Override
    public <T extends ManageableResource> List<T> findChildResources(Class<T> resourceType) throws IOException {
        return findChildResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> List<T> findChildResources(Class<T> resourceType, String resourceName) throws IOException {
        final List<T> result = new ArrayList<>();
        for (Factory<?> childResourceFactory : childResourceFactories.values()) {
            // add local children if target resource type is super of the local children type
            if (childResourceFactory.getResourceType().getType().isInstance(resourceType)) {
                final Factory<T> tChildResourceFactory = (Factory<T>) childResourceFactory;
                if (resourceName == null) {
                    result.addAll(tChildResourceFactory.getResources());
                } else {
                    result.add(tChildResourceFactory.getResource(resourceName));
                }
            }
            // search local children if target resource type is a super of the local children child types
            boolean findRecursive = false;
            for (Type childrenChildType : childResourceFactory.getResourceType().getChildTypes(true)) {
                if (childrenChildType.getType().isInstance(resourceType)) {
                    findRecursive = true;
                    break;
                }
            }
            if (findRecursive) {
                for (ManageableResource childResource : childResourceFactory.getResources()) {
                    result.addAll(childResource.findChildResources(resourceType, resourceName));
                }
            }
        }
        return result;
    }

    @Override
    public <T extends ManageableResource> List<T> findChildResources(Query<T> query) throws IOException {
        final ManageableResource resourceToQuery = query.isRunFromRoot() ? getServerConfiguration().getRootResource() : this;
        final Query<?> parentQuery = query.getParent();
        if (parentQuery != null) {
            final L
        } else {

        }
        if (query.getParent())
        if (query.isRunFromRoot()) {

        }
    }

    protected static class Type<T extends ManageableResource> {

        private final Class<T> type;
        private final Type[] childTypes;
        private final Type[] childTypesRecursive;

        public Type(Class<T> type, Type... childTypes) {
            this.type = type;
            this.childTypes = childTypes;
            this.childTypesRecursive = Stream.of(childTypes)
                    .flatMap(childType -> Stream.of(childType.getChildTypes(true)))
                    .toArray(Type[]::new);
        }

        public Class<T> getType() {
            return type;
        }

        public Type[] getChildTypes(boolean recursive) {
            return recursive ? childTypesRecursive : childTypes;
        }
    }

    protected static abstract class Factory<T extends ManageableResource> {

        protected final ManageableResource parentResource;
        protected final ManageableServerConfiguration serverConfiguration;

        protected final PathAddress pathAddressBase;
        protected final String pathElementKey;
        protected final Type<T> resourceType;

        public Factory(Type<T> resourceType, PathAddress pathAddressBase, String pathElementKey, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            this.resourceType = resourceType;
            this.pathAddressBase = pathAddressBase;
            this.pathElementKey = pathElementKey;
            this.parentResource = parentResource;
            this.serverConfiguration = serverConfiguration;
        }

        public PathAddress getResourcePathAddress(String resourceName) {
            return pathAddressBase.append(pathElementKey, resourceName);
        }

        public Set<String> getResourceNames() throws IOException {
            try {
                final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, pathAddressBase);
                op.get(CHILD_TYPE).set(pathElementKey);
                final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
                Set<String> result = new HashSet<>();
                for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                    result.add(resultNode.asString());
                }
                return result;
            } catch (ManagementOperationException e) {
                try {
                    final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_TYPES_OPERATION, pathAddressBase);
                    final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
                    boolean childrenTypeFound = false;
                    for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                        if (pathElementKey.equals(resultNode.asString())) {
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

        public void removeResource(String resourceName) throws IOException {
            final PathAddress address = getResourcePathAddress(resourceName);
            final ModelNode op = Util.createRemoveOperation(address);
            serverConfiguration.executeManagementOperation(op);
        }

        public Type<T> getResourceType() {
            return resourceType;
        }

        public T getResource(String resourceName) throws IOException {
            return getResourceNames().contains(resourceName) ? newResourceInstance(resourceName) : null;
        }

        public List<T> getResources() throws IOException {
            final Set<String> resourceNames = getResourceNames();
            if (resourceNames.isEmpty()) {
                return Collections.emptyList();
            } else {
                final List<T> result = new ArrayList<>();
                for (String resourceName : resourceNames) {
                    result.add(getResource(resourceName));
                }
                return result;
            }
        }

        protected abstract T newResourceInstance(String resourceName);
    }
}
