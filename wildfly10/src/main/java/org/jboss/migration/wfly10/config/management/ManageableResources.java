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

import java.util.List;

/**
 * @author emmartins
 */
public interface ManageableResourcesChildren {

    List<ManageableResource> getAllChildResources();

    List<ManageableResource> getChildResourcesByName(String resourceName);

    <T extends ManageableResource> List<T> getChildResourcesByType(Class<T> resourceType);

    <T extends ManageableResource> List<T> getChildResourcesByTypeAndName(Class<T> resourceType, String resourceName);

    <T extends ManageableResource> List<T> queryChildResources(Query<T> query);

    interface Query<T extends ManageableResource> {
        Class<T> getResourceType();
        String getResourceName();
        Query<?> getParentsQuery();
    }
}
