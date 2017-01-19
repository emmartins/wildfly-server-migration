package org.jboss.migration.wfly10.config.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public interface ManageableResourceUtils {

    static <R1 extends ManageableResource, R2 extends ManageableResource> List<R2> findChildResources(List<R1> resourceList, ManageableResource.Type<R2> childResourcesType) throws IOException {
        List<R2> children = new ArrayList<>();
        for (R1 resource : resourceList) {
            for(ManageableResources<R2> childResources : resource.findResources(childResourcesType)) {
                children.addAll(childResources.getResources());
            }
        }
        return children;
    }

    static <R1 extends ManageableResource, R2 extends ManageableResource> List<R2> findChildResources(List<ManageableResources<R1>> resourcesList, ManageableResource.Type<R2> childResourcesType) throws IOException {
        List<R2> children = new ArrayList<>();
        for (ManageableResources<R1> resources : resourcesList) {
            for (R1 resource : resources.getResources()) {
                for (ManageableResources<R2> childResources : resource.findResources(childResourcesType)) {
                    children.addAll(childResources.getResources());
                }
            }
        }
        return children;
    }
}
