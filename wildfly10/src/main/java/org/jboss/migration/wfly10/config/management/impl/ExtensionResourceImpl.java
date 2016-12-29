package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ExtensionResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;

/**
 * @author emmartins
 */
public class ExtensionResourceImpl extends AbstractManageableResource<ExtensionResource.Parent> implements ExtensionResource {

    private ExtensionResourceImpl(String resourceName, PathAddress pathAddress, ExtensionResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<ExtensionResource, ExtensionResource.Parent> {
        public Factory(PathAddress pathAddressBase, ExtensionResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, EXTENSION, parentResource);
        }
        @Override
        public ExtensionResource newResourceInstance(String resourceName) {
            return new ExtensionResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
