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
import org.jboss.migration.wfly10.config.management.ManageableResourceType;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractManageableResource<P extends ManageableResource> implements ManageableResource {

    private final Map<ManageableResourceType, Factory> childResourceFactories = new HashMap<>();

    private final String resourceName;
    private final PathAddress pathAddress;
    private final P parent;
    private final ManageableServerConfiguration serverConfiguration;

    protected AbstractManageableResource(String resourceName, PathAddress pathAddress, P parent) {
        this.resourceName = resourceName;
        this.pathAddress = pathAddress != null ? pathAddress : PathAddress.EMPTY_ADDRESS;
        this.parent = parent;
        this.serverConfiguration = parent != null ? parent.getServerConfiguration() : (ManageableServerConfiguration) this;
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
    public P getParentResource() {
        return parent;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public ModelNode getResourceConfiguration() {
        // ensure resource exists
        if (!isExistentResource()) {
            return null;
        }
        // get resource
        final PathAddress address = getResourcePathAddress();
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = serverConfiguration.executeManagementOperation(op);
        return result.get(RESULT);
    }

    protected void addChildResourceFactory(Factory childResourceFactory) {
        childResourceFactories.put(childResourceFactory.getResourceType(), childResourceFactory);
    }

    protected <T extends ManageableResource> Factory<T, ?> getChildResourceFactory(ManageableResourceType resourceType) {
        return childResourceFactories.get(resourceType);
    }

    protected <T extends ManageableResource> List<Factory> getChildResourceFactories(Class<T> resourceType) {
        return childResourceFactories.values().stream().filter(factory -> isResourceTypeMatch(resourceType, factory.getResourceType().getType())).collect(toList());
    }

    protected <T extends ManageableResource> List<Factory> getDescendantResourceFactories(ManageableResourceType resourceType) {
        return childResourceFactories.values().stream().filter(factory -> factory.getResourceType().getDescendantTypes().contains(resourceType)).collect(toList());
    }

    private boolean isResourceTypeMatch(Class<?> resourceType, Class<?> candidateType) {
        if (resourceType.equals(candidateType)) {
            return true;
        }
        for (Class<?> candidateInterfaceType : candidateType.getInterfaces()) {
            if (isResourceTypeMatch(resourceType, candidateInterfaceType)) {
                return true;
            }
        }
        return false;
    }
    protected <T extends ManageableResource> List<Factory> getDescendantResourceFactories(Class<T> resourceType) {
        return childResourceFactories.values().stream().filter(factory -> {
            final Set<ManageableResourceType> descendantTypes = factory.getResourceType().getDescendantTypes();
            for (ManageableResourceType t : descendantTypes) {
                if (isResourceTypeMatch(resourceType, t.getType())) {
                    return true;
                }
            }
            return false;
        }).collect(toList());
    }

    @Override
    public <T extends ManageableResource> T getChildResource(ManageableResourceType resourceType, String resourceName) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResource(resourceName) : null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(ManageableResourceType resourceType) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResources() : null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType) {
        return getChildResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType, String resourceName) {
        final List<Factory> factories = getChildResourceFactories(resourceType);
        if (factories.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<T> result = new ArrayList<T>();
            for (Factory<T, ?> factory : factories) {
                if (resourceName != null) {
                    final T t = factory.getResource(resourceName);
                    if (t != null) {
                        result.add(t);
                    }
                } else {
                    result.addAll(factory.getResources());
                }
            }
            return result;
        }
    }

    @Override
    public Set<ManageableResourceType> getChildResourceTypes() {
        return Collections.unmodifiableSet(childResourceFactories.keySet());
    }

    @Override
    public Set<String> getChildResourceNames(ManageableResourceType resourceType) {
        final Factory factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResourceNames() : null;
    }

    @Override
    public <T extends ManageableResource> PathAddress getChildResourcePathAddress(ManageableResourceType resourceType, String resourceName) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResourcePathAddress(resourceName) : null;
    }

    @Override
    public void removeChildResource(ManageableResourceType resourceType, String resourceName) {
        final Factory<?, ?> factory = getChildResourceFactory(resourceType);
        if (factory != null) {
            factory.removeResource(resourceName);
        }
    }

    @Override
    public void removeResource() throws ManagementOperationException {
        if (isExistentResource()) {
            final PathAddress address = getResourcePathAddress();
            final ModelNode op = Util.createRemoveOperation(address);
            serverConfiguration.executeManagementOperation(op);
        }
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(ManageableResourceType resourceType) {
        return findResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(ManageableResourceType resourceType, String resourceName) {
        final Set<T> result = new HashSet<>();
        // this
        if (resourceType.equals(getResourceType()) && (resourceName == null || resourceName.equals(getResourceName()))) {
            result.add((T) this);
        }
        // children
        final Factory<?, ?> childFactory = getChildResourceFactory(resourceType);
        if (childFactory != null) {
            if (resourceName != null) {
                final ManageableResource child = childFactory.getResource(resourceName);
                if (child != null) {
                    result.add((T) child);
                }
            } else {
                for (ManageableResource child : childFactory.getResources()) {
                    result.add((T) child);
                }
            }
        }
        // descendants
        for(Factory<?, ?> descendantFactory : getDescendantResourceFactories(resourceType)) {
            for (ManageableResource child : descendantFactory.getResources()) {
                result.addAll(child.findResources(resourceType, resourceName));
            }
        }
        return result;
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Class<T> resourceType) {
        return findResources(resourceType, null);
    }

    @Override
    public String toString() {
        return "ManageableResource(type="+String.valueOf(getResourceType())+", name="+String.valueOf(getResourceName())+")";
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Class<T> resourceType, String resourceName) {
        final Set<T> result = new HashSet<>();
        // this
        if (resourceType.isInstance(this) && (resourceName == null || resourceName.equals(getResourceName()))) {
            result.add((T) this);
        }

        // children
        for(Factory<?, ?> childFactory : getChildResourceFactories(resourceType)) {
            if (resourceName != null) {
                final ManageableResource child = childFactory.getResource(resourceName);
                if (child != null) {
                    result.add((T) child);
                }
            } else {
                for (ManageableResource child : childFactory.getResources()) {
                    result.add((T) child);
                }
            }
        }

        // descendants
        for(Factory<?, ?> descendantFactory : getDescendantResourceFactories(resourceType)) {
            for (ManageableResource child : descendantFactory.getResources()) {
                result.addAll(child.findResources(resourceType, resourceName));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractManageableResource that = (AbstractManageableResource) o;

        if (!pathAddress.equals(that.pathAddress)) return false;
        return serverConfiguration.equals(that.serverConfiguration);
    }

    @Override
    public int hashCode() {
        return pathAddress.hashCode();
    }

    protected abstract static class Factory<T extends ManageableResource, P extends ManageableResource> {

        protected final P parentResource;
        protected final ManageableServerConfiguration serverConfiguration;

        protected final PathAddress pathAddressBase;
        protected final String pathElementKey;
        protected final ManageableResourceType resourceType;

        public Factory(ManageableResourceType resourceType, PathAddress pathAddressBase, String pathElementKey, P parentResource) {
            this.resourceType = resourceType;
            this.pathAddressBase = pathAddressBase;
            this.pathElementKey = pathElementKey;
            this.parentResource = parentResource;
            this.serverConfiguration = parentResource.getServerConfiguration();
        }

        public PathAddress getResourcePathAddress(String resourceName) {
            return pathAddressBase.append(pathElementKey, resourceName);
        }

        public Set<String> getResourceNames() {
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

        public boolean hasResource(String resourceName) {
            return getResourceNames().contains(resourceName);
        }

        public void removeResource(String resourceName) {
            if (hasResource(resourceName)) {
                final PathAddress address = getResourcePathAddress(resourceName);
                final ModelNode op = Util.createRemoveOperation(address);
                serverConfiguration.executeManagementOperation(op);
            }
        }

        public ManageableResourceType getResourceType() {
            return resourceType;
        }

        public T getResource(String resourceName) {
            return hasResource(resourceName) ? newResourceInstance(resourceName) : null;
        }

        public List<T> getResources() {
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

        @Override
        public String toString() {
            return "ManageableResourceChildFactory(parent="+String.valueOf(parentResource)+", childType="+getResourceType()+")";
        }
    }
}
