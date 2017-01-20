package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.management.SystemPropertyResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTY;

/**
 * @author emmartins
 */
public class SystemPropertyResourceImpl extends ManageableResourceImpl implements SystemPropertyResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(SocketBindingResource.class);

    private SystemPropertyResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<SystemPropertyResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, SYSTEM_PROPERTY, parentResource, serverConfiguration);
        }
        @Override
        public SystemPropertyResource newResourceInstance(String resourceName) {
            return new SystemPropertyResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
