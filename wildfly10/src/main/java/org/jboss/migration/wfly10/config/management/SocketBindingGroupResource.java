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

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public interface SocketBindingGroupResource extends ManageableResource, SocketBindingResource.Parent {

    Type<SocketBindingGroupResource> RESOURCE_TYPE = new Type<>(SocketBindingGroupResource.class, SocketBindingResource.RESOURCE_TYPE);

    @Override
    default Type<SocketBindingGroupResource> getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * A facade (with full defaults) for a {@link ManageableResource} which has {@link SocketBindingGroupResource} children.
     */
    interface Parent extends ManageableResource {
        default SocketBindingGroupResource getSocketBindingGroupResource(String resourceName) throws IOException {
            return getChildResource(RESOURCE_TYPE, resourceName);
        }
        default List<SocketBindingGroupResource> getSocketBindingGroupResources() throws IOException {
            return getChildResources(RESOURCE_TYPE);
        }
        default Set<String> getSocketBindingGroupResourceNames() throws IOException {
            return getChildResourceNames(RESOURCE_TYPE);
        }
        default PathAddress getSocketBindingGroupResourcePathAddress(String resourceName) {
            return getChildResourcePathAddress(RESOURCE_TYPE, resourceName);
        }
        default String getSocketBindingGroupResourceAbsoluteName(String resourceName) {
            return getChildResourcePathAddress(RESOURCE_TYPE, resourceName).toCLIStyleString();
        }
        default void removeSocketBindingGroupResource(String resourceName) throws IOException {
            removeResource(RESOURCE_TYPE, resourceName);
        }
    }
}
