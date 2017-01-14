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

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableResourceWithChildren;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author emmartins
 */
public class ManageableResourcesImpl implements ManageableResources {

    protected final List<ChildrenCollection> collections;
    protected final Map<Class<? extends ManageableResource>, List<ChildrenCollection>> collectionsByType;

    protected ManageableResourcesImpl(List<ChildrenCollection> collections) {
        this.collections = Collections.unmodifiableList(collections);
        this.collectionsByType = collections.stream().collect(Collectors.groupingBy(ChildrenCollection::getResourcesType));
    }

    protected <T extends ManageableResource> List<ChildrenCollection> getChildrenCollections(Class<T> resourceType) {
        if (resourceType == null || resourceType == ManageableResource.class) {
            // return all
            return collections;
        } else {
            return collectionsByType.get(resourceType);
        }
    }

    @Override
    public List<ManageableResource> getAllChildResources() {
        return getChildResources(null);
    }

    @Override
    public <T extends ManageableResource> List<T> getByName(String resourceName) {
        return getChildResourcesByTypeAndName(null, resourceName);
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResourcesByType(Class<T> resourceType) {
        return getChildResourcesByTypeAndName(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResourcesByTypeAndName(Class<T> resourceType, String resourceName) {
        List<T> childResources = getChildResources(getChildrenCollections(resourceType));
        if (resourceName != null) {
            childResources = childResources.stream().filter(child -> child.getResourceName().equals(resourceName)).collect(Collectors.toList());
        }
        return childResources;
    }

    protected <T extends ManageableResource> List<T> getChildResources(List<ChildrenCollection> childrenCollections) {
        if (childrenCollections == null) {
            return Collections.emptyList();
        }
        return (List<T>) childrenCollections.stream()
                // get resources for each children
                .map(childrenCollection -> childrenCollection.getResources())
                // merge each children results
                .collect(ArrayList::new, List::addAll, List::addAll);
    }

    @Override
    public <T extends ManageableResource> List<T> get(Query<T> query) {
        final Query<? extends ManageableResourceWithChildren> parentsQuery = query.getParentsQuery();
        if (parentsQuery != null) {
            final List<? extends ManageableResourceWithChildren> parents = get(parentsQuery);
            if (parents == null || parents.isEmpty()) {
                return Collections.emptyList();
            } else {
                return parents.stream().map(parent -> parent.get(query)).collect(ArrayList::new, List::addAll, List::addAll);
            }
        } else {
            return getChildResourcesByTypeAndName(query.getResourceType(), query.getResourceName(), query.isRecursive());
        }
    }

    protected interface ChildrenCollection<T extends ManageableResource> {
        Class<T> getResourcesType();
        List<T> getResources();
    }
}
