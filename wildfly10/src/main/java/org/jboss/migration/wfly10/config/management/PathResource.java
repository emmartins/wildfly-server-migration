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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public interface PathResource extends ManageableResource {

    ManageableResourceType RESOURCE_TYPE = new ManageableResourceType(PathResource.class);

    @Override
    default ManageableResourceType getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    Parent getParentResource();

    /**
     * A facade (with full defaults) for a {@link ManageableResource} which has {@link PathResource} children.
     */
    interface Parent extends ManageableResource {
        default PathResource getPathResource(String resourceName) throws ManagementOperationException {
            return getChildResource(RESOURCE_TYPE, resourceName);
        }
        default List<PathResource> getPathResources() throws ManagementOperationException {
            return getChildResources(RESOURCE_TYPE);
        }
        default Set<String> getPathResourceNames() throws ManagementOperationException {
            return getChildResourceNames(RESOURCE_TYPE);
        }
        default PathAddress getPathResourcePathAddress(String resourceName) {
            return getChildResourcePathAddress(RESOURCE_TYPE, resourceName);
        }
        default String getPathResourceAbsoluteName(String resourceName) {
            return getChildResourcePathAddress(RESOURCE_TYPE, resourceName).toCLIStyleString();
        }
        default ModelNode getPathResourceConfiguration(String resourceName) throws ManagementOperationException {
            return getChildResourceConfiguration(RESOURCE_TYPE, resourceName);
        }
        default boolean hasPathResource(String resourceName) throws ManagementOperationException {
            return hasChildResource(RESOURCE_TYPE, resourceName);
        }
        default void removePathResource(String resourceName) throws ManagementOperationException {
            removeChildResource(RESOURCE_TYPE, resourceName);
        }
    }
}
