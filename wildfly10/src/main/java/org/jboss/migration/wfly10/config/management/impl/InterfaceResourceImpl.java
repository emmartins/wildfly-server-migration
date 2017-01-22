package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;

/**
 * @author emmartins
 */
public class InterfaceResourceImpl extends AbstractManageableResource implements InterfaceResource {

    private InterfaceResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<InterfaceResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, INTERFACE, parentResource);
        }
        @Override
        public InterfaceResource newResourceInstance(String resourceName) {
            return new InterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
