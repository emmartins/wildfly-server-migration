package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.InterfaceResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;

/**
 * @author emmartins
 */
public class InterfaceResourceImpl extends AbstractManageableResource<InterfaceResource.Parent> implements InterfaceResource {

    private InterfaceResourceImpl(String resourceName, PathAddress pathAddress, InterfaceResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<InterfaceResource, InterfaceResource.Parent> {
        public Factory(PathAddress pathAddressBase, InterfaceResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, INTERFACE, parentResource);
        }
        @Override
        public InterfaceResource newResourceInstance(String resourceName) {
            return new InterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
