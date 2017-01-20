package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.HostResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;

/**
 * @author emmartins
 */
public class HostResourceImpl extends ManageableResourceImpl implements HostResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(HostResource.class);

    private HostResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<HostResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, HOST, parentResource, serverConfiguration);
        }
        @Override
        public HostResource newResourceInstance(String resourceName) {
            return new HostResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
