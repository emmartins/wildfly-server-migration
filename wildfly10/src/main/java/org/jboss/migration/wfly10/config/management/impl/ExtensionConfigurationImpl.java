package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ExtensionConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;

/**
 * @author emmartins
 */
public class ExtensionConfigurationImpl extends ManageableResourceImpl implements ExtensionConfiguration {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(ExtensionConfiguration.class);

    private ExtensionConfigurationImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<ExtensionConfiguration> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, EXTENSION, parentResource, serverConfiguration);
        }
        @Override
        public ExtensionConfiguration newResourceInstance(String resourceName) {
            return new ExtensionConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
