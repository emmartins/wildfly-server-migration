package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.SystemPropertyResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTY;

/**
 * @author emmartins
 */
public class SystemPropertyResourceImpl extends AbstractManageableResource<SystemPropertyResource.Parent> implements SystemPropertyResource {


    private SystemPropertyResourceImpl(String resourceName, PathAddress pathAddress, SystemPropertyResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SystemPropertyResource, SystemPropertyResource.Parent> {
        public Factory(PathAddress pathAddressBase, SystemPropertyResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SYSTEM_PROPERTY, parentResource);
        }
        @Override
        public SystemPropertyResource newResourceInstance(String resourceName) {
            return new SystemPropertyResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
