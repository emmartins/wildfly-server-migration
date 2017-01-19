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

/**
 * @author emmartins
 */
public interface ManageableResource {

    // resource
    String getResourceName();
    PathAddress getResourcePathAddress();
    ModelNode getResourceConfiguration() throws IOException;

    // children
    <T extends ManageableResource> T getChildResource(Class<T> resourceType, String resourceName) throws IOException;
    <T extends ManageableResource> Set<T> getChildResources(Class<T> resourceType) throws IOException;
    Set<Class<? extends ManageableResource>> getChildResourceTypes();
    Set<String> getChildResourceNames(Class<? extends ManageableResource> resourceType) throws IOException;
    <T extends ManageableResource> PathAddress getChildResourcePathAddress(Class<T> resourceType, String resourceName);
    <T extends ManageableResource> Set<T> findChildResources(Class<T> resourceType) throws IOException;
    <T extends ManageableResource> Set<T> findChildResources(Class<T> resourceType, String resourceName) throws IOException;
    void removeResource(Class<? extends ManageableResource> resourceType, String resourceName) throws IOException;
    //ModelNode getResourceConfiguration(String name) throws IOException;

    // parent
    ManageableResource getParent();
    ManageableServerConfiguration getServerConfiguration();

    // TODO move to impl
    interface Type<T extends ManageableResource> {
        Class<T> getType();
        Type<?>[] getChildTypes(boolean recursive);
    }

}
