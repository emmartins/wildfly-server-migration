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

import org.jboss.migration.wfly10.config.management.ManageableNode;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ChildResources;
import org.jboss.migration.wfly10.config.management.ManageableResourceWithChildren;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author emmartins
 */
public abstract class ManageableResourcesFinderImpl implements ChildResources {


    protected <T extends ManageableResource> List<ChildResources> getChildrenCollections(Class<T> resourceType) {
        if (resourceType == null || resourceType == ManageableResource.class) {
            // return all
            return collections;
        } else {
            return collectionsByType.get(resourceType);
        }
    }


    @Override
    public <T extends ManageableResource> List<T> getChildResourcesByType(Class<T> resourceType) {
        return findResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> List<T> findResources(Class<T> type, String resourceName) {
        List<T> childResources = getChildResources(getChildrenCollections(type));
        if (resourceName != null) {
            childResources = childResources.stream().filter(child -> child.getResourceName().equals(resourceName)).collect(Collectors.toList());
        }
        return childResources;
    }

    protected <T extends ManageableResource> List<T> getChildResources(List<ChildResources> childResources) {
        if (childResources == null) {
            return Collections.emptyList();
        }
        return (List<T>) childResources.stream()
                // get resources for each children
                .map(manageableResourcesFinder -> manageableResourcesFinder.getResources())
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
            return findResources(query.getResourceType(), query.getResourceName(), query.isRecursive());
        }
    }

    @Override
    public <T extends ManageableResources> List<T> findResources(Class<T> type) {
        return null;
    }

    @Override
    public <T extends ManageableNode> List<T> findResources(Query<T> query) {
        return null;
    }
}
