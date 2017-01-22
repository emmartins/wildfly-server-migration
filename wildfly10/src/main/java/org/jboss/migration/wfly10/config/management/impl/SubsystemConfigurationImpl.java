package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author emmartins
 */
public class SubsystemConfigurationImpl extends AbstractManageableResource implements SubsystemConfiguration {

    private SubsystemConfigurationImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SubsystemConfiguration> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SUBSYSTEM, parentResource);
        }
        @Override
        public SubsystemConfiguration newResourceInstance(String resourceName) {
            return new SubsystemConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
