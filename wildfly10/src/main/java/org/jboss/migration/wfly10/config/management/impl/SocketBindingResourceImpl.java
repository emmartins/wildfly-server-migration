package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;

/**
 * @author emmartins
 */
public class SocketBindingResourceImpl extends ManageableResourceImpl implements SocketBindingResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(SocketBindingResource.class);

    private SocketBindingResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<SocketBindingResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, SOCKET_BINDING, parentResource, serverConfiguration);
        }
        @Override
        public SocketBindingResource newResourceInstance(String resourceName) {
            return new SocketBindingResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
