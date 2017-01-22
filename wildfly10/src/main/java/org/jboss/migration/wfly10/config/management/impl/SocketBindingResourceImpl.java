package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;

/**
 * @author emmartins
 */
public class SocketBindingResourceImpl extends AbstractManageableResource implements SocketBindingResource {

    private SocketBindingResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SocketBindingResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SOCKET_BINDING, parentResource);
        }
        @Override
        public SocketBindingResource newResourceInstance(String resourceName) {
            return new SocketBindingResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
