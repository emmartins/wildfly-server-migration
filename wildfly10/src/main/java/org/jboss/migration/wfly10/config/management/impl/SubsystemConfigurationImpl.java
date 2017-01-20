package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.JVM;

/**
 * @author emmartins
 */
public class SubsystemConfigurationImpl extends ManageableResourceImpl implements SubsystemConfiguration {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(SubsystemConfiguration.class);

    private SubsystemConfigurationImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<SubsystemConfiguration> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, JVM, parentResource, serverConfiguration);
        }
        @Override
        public SubsystemConfiguration newResourceInstance(String resourceName) {
            return new SubsystemConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
