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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author emmartins
 */
public interface ManageableResource {

    // resource
    <T extends ManageableResource> Type<T> getResourceType();
    String getResourceName();

    default String getResourceAbsoluteName() {
        return getResourcePathAddress().toCLIStyleString();
    }

    PathAddress getResourcePathAddress();
    ModelNode getResourceConfiguration() throws ManagementOperationException;

    // children
    <T extends ManageableResource> T getChildResource(Type<T> resourceType, String resourceName) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(Type<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType, String resourceName) throws ManagementOperationException;
    Set<Type<?>> getChildResourceTypes();
    Set<String> getChildResourceNames(Type<?> resourceType) throws ManagementOperationException;

    default <T extends ManageableResource> String getChildResourceAbsoluteName(Type<T> resourceType, String resourceName) {
        return getChildResourcePathAddress(resourceType, resourceName).toCLIStyleString();
    }
    <T extends ManageableResource> PathAddress getChildResourcePathAddress(Type<T> resourceType, String resourceName);
    <T extends ManageableResource> Set<T> findResources(Type<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(Class<T> resourceType) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(Type<T> resourceType, String resourceName) throws ManagementOperationException;
    <T extends ManageableResource> Set<T> findResources(Class<T> resourceType, String resourceName) throws ManagementOperationException;

    void removeResource(Type<?> resourceType, String resourceName) throws ManagementOperationException;
    //ModelNode getResourceConfiguration(String name) throws IOException;

    // parent
    ManageableResource getParentResource();
    ManageableServerConfiguration getServerConfiguration();

    class Type<T extends ManageableResource> {

        private final Class<T> type;
        private final Set<Type<?>> childTypes;
        private final Set<Type<?>> descendantTypes;

        protected Type(Class<T> type, Type<?>... childTypes) {
            this.type = type;
            final Stream<Type<?>> childTypesStream = Stream.of(childTypes);
            this.childTypes = childTypesStream.collect(toSet());
            this.descendantTypes = childTypesStream
                    .flatMap(childType -> Stream.concat(Stream.of(childType), childType.getDescendantTypes().stream()))
                    .collect(toSet());
        }

        public Class<T> getType() {
            return type;
        }

        public Set<Type<?>> getChildTypes() {
            return childTypes;
        }

        public Set<Type<?>> getDescendantTypes() {
            return descendantTypes;
        }
    }

}
