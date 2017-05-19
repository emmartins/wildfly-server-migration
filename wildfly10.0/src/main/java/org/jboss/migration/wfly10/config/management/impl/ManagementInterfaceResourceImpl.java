package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_INTERFACE;

/**
 * @author emmartins
 */
public class ManagementInterfaceResourceImpl extends AbstractManageableResource<ManagementInterfaceResource.Parent> implements ManagementInterfaceResource {

    private ManagementInterfaceResourceImpl(String resourceName, PathAddress pathAddress, ManagementInterfaceResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<ManagementInterfaceResource, ManagementInterfaceResource.Parent> {
        public Factory(PathAddress pathAddressBase, ManagementInterfaceResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, MANAGEMENT_INTERFACE, parentResource);
        }
        @Override
        public ManagementInterfaceResource newResourceInstance(String resourceName) {
            return new ManagementInterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
