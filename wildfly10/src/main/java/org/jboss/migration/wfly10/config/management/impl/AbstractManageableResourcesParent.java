package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ManageableResourcesParent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emmartins
 */
public abstract class AbstractManageableResourcesParent implements ManageableResourcesParent {

    private final Map<ManageableResource.Type, ManageableResources> childResourcesMap = new HashMap<>();

    protected void addChildResources(ManageableResources childResources) {
        childResourcesMap.put(childResources.getResourceType(), childResources);
    }

    @Override
    public <T extends ManageableResource> ManageableResources<T> getResources(ManageableResource.Type<T> resourceType) {
        return childResourcesMap.get(resourceType);
    }

    @Override
    public <T extends ManageableResource> List<ManageableResources<T>> findResources(ManageableResource.Type<T> resourceType) throws IOException {
        final List<ManageableResources<T>> result = new ArrayList<>();
        for (ManageableResources<?> childResources : childResourcesMap.values()) {
            if (childResources.getResourceType() == resourceType) {
                result.add((ManageableResources<T>) childResources);
            } else {
                boolean findRecursive = false;
                for (ManageableResource.Type type : childResources.getResourceType().getChildTypes(true)) {
                    if (type == resourceType) {
                        findRecursive = true;
                        break;
                    }
                }
                if (findRecursive) {
                    for (ManageableResource childResource : childResources.getResources()) {
                        result.addAll(childResource.findResources(resourceType));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T extends ManageableResource> List<T> findResources(ManageableResource.Type<T> resourceType, String resourceName) throws IOException {
        final List<T> result = new ArrayList<>();
        for (ManageableResources<T> resources : findResources(resourceType)) {
            final T resource = resources.getResource(resourceName);
            if (resource != null) {
                result.add(resource);
            }
        }
        return result;
    }
}
