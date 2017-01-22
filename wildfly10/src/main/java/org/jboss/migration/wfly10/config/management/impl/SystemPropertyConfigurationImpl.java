package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SystemPropertyConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTY;

/**
 * @author emmartins
 */
public class SystemPropertyConfigurationImpl extends AbstractManageableResource implements SystemPropertyConfiguration {


    private SystemPropertyConfigurationImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SystemPropertyConfiguration> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SYSTEM_PROPERTY, parentResource);
        }
        @Override
        public SystemPropertyConfiguration newResourceInstance(String resourceName) {
            return new SystemPropertyConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
