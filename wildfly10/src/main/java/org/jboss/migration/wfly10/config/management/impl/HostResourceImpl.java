package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.HostResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;

/**
 * @author emmartins
 */
public class HostResourceImpl extends AbstractManageableResource implements HostResource {

    private HostResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<HostResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, HOST, parentResource);
        }
        @Override
        public HostResource newResourceInstance(String resourceName) {
            return new HostResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
