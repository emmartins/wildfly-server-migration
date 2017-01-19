package org.jboss.migration.wfly10.config.management;

import java.io.IOException;
import java.util.List;

/**
 * @author emmartins
 */
public interface ManageableResourcesParent {
    <T extends ManageableResource> ManageableResources<T> getResources(ManageableResource.Type<T> resourceType);
    <T extends ManageableResource> List<ManageableResources<T>> findResources(ManageableResource.Type<T> resourcesType) throws IOException;
    <T extends ManageableResource> List<T> findResources(ManageableResource.Type<T> resourceType, String resourceName) throws IOException;
}
