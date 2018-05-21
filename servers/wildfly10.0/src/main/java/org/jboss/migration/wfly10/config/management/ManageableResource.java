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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public interface ManageableResource {

    // resource
    ManageableResourceType getResourceType();
    String getResourceName();
    default String getResourceAbsoluteName() {
        return getResourcePathAddress().toCLIStyleString();
    }
    PathAddress getResourcePathAddress();
    ModelNode getResourceConfiguration() throws ManagementOperationException;
    void removeResource() throws ManagementOperationException;
    default boolean isExistentResource() throws ManagementOperationException {
        final ManageableResource parent = getParentResource();
        return parent != null ? parent.hasChildResource(getResourceType(), getResourceName()) : true;
    }

    // parent
    ManageableResource getParentResource();
    ManageableServerConfiguration getServerConfiguration();

    // children
    <T extends ManageableResource> T getChildResource(ManageableResourceType resourceType, String resourceName) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(ManageableResourceType resourceType) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType, String resourceName) throws ManagementOperationException;
    default Set<ManageableResourceType> getChildResourceTypes() {
        return getResourceType().getChildTypes();
    }
    Set<String> getChildResourceNames(ManageableResourceType resourceType) throws ManagementOperationException;
    default <T extends ManageableResource> String getChildResourceAbsoluteName(ManageableResourceType resourceType, String resourceName) {
        return getChildResourcePathAddress(resourceType, resourceName).toCLIStyleString();
    }
    <T extends ManageableResource> PathAddress getChildResourcePathAddress(ManageableResourceType resourceType, String resourceName);
    default void removeChildResource(ManageableResourceType resourceType, String resourceName) throws ManagementOperationException {
        ManageableResource child = getChildResource(resourceType, resourceName);
        if (child != null) {
            child.removeResource();
        }
    }
    default ModelNode getChildResourceConfiguration(ManageableResourceType resourceType, String resourceName) throws ManagementOperationException {
        ManageableResource child = getChildResource(resourceType, resourceName);
        return child != null ? child.getResourceConfiguration() : null;
    }
    default boolean hasChildResource(ManageableResourceType resourceType, String resourceName) throws ManagementOperationException {
        return getChildResourceNames(resourceType).contains(resourceName);
    }

    // queries
    <T extends ManageableResource> Set<T> findResources(ManageableResourceType resourceType) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(Class<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(ManageableResourceType resourceType, String resourceName) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(Class<T> resourceType, String resourceName) throws ManagementOperationException;
}
