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

import java.util.Collections;
import java.util.HashSet;

/**
 * @author emmartins
 */
public interface ManageableResourceSelectors {

    static <T extends ManageableServerConfiguration> ManageableResourceSelector<T> selectServerConfiguration() {
        return resource -> Collections.singleton((T) resource.getServerConfiguration());
    }

    static ManageableResourceSelector<ManageableResource> toParent() {
        return resource -> {
            final ManageableResource parent = resource.getParentResource();
            return parent != null ? Collections.singleton(parent) : Collections.emptySet();
        };
    }

    static <T extends ManageableResource> ManageableResourceSelector<T> selectResources(Class<T> resourceType) {
        return resource -> resource.findResources(resourceType);
    }

    static <T extends ManageableResource> ManageableResourceSelector<T> selectResources(Class<T> resourceType, String resourceName) {
        return resource -> resource.findResources(resourceType, resourceName);
    }

    static <T extends ManageableResource> ManageableResourceSelector<T> toChildren(Class<T> resourceType) {
        return resource -> new HashSet<>(resource.getChildResources(resourceType));
    }

    static <T extends ManageableResource> ManageableResourceSelector<T> toChild(ManageableResource.Type<T> resourceType, String resourceName) {
        return resource -> {
            final T child = resource.getChildResource(resourceType, resourceName);
            return child != null ? Collections.singleton(child) : Collections.emptySet();
        };
    }
}
