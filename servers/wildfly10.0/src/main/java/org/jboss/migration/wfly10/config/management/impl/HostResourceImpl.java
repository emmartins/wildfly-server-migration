package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.HostResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;

/**
 * @author emmartins
 */
public class HostResourceImpl extends AbstractManageableResource<HostResource.Parent> implements HostResource {

    private HostResourceImpl(String resourceName, PathAddress pathAddress, HostResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<HostResource, HostResource.Parent> {
        public Factory(PathAddress pathAddressBase, HostResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, HOST, parentResource);
        }
        @Override
        public HostResource newResourceInstance(String resourceName) {
            return new HostResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
