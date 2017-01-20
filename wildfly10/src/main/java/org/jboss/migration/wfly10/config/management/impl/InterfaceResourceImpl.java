package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;

/**
 * @author emmartins
 */
public class InterfaceResourceImpl extends ManageableResourceImpl implements InterfaceResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(InterfaceResource.class);

    private InterfaceResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<InterfaceResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, INTERFACE, parentResource, serverConfiguration);
        }
        @Override
        public InterfaceResource newResourceInstance(String resourceName) {
            return new InterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
