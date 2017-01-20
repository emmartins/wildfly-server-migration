package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_INTERFACE;

/**
 * @author emmartins
 */
public class ManagementInterfaceResourceImpl extends ManageableResourceImpl implements ManagementInterfaceResource {
    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(ManagementInterfaceResource.class);

    private ManagementInterfaceResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<ManagementInterfaceResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, MANAGEMENT_INTERFACE, parentResource, serverConfiguration);
        }
        @Override
        public ManagementInterfaceResource newResourceInstance(String resourceName) {
            return new ManagementInterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
