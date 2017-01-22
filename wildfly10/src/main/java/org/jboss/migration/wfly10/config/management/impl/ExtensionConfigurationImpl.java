package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ExtensionConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;

/**
 * @author emmartins
 */
public class ExtensionConfigurationImpl extends AbstractManageableResource implements ExtensionConfiguration {

    private ExtensionConfigurationImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<ExtensionConfiguration> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, EXTENSION, parentResource);
        }
        @Override
        public ExtensionConfiguration newResourceInstance(String resourceName) {
            return new ExtensionConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
