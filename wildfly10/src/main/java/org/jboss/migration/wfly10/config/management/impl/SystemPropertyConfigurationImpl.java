package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.SystemPropertyConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTY;

/**
 * @author emmartins
 */
public class SystemPropertyConfigurationImpl extends AbstractManageableResource<SystemPropertyConfiguration.Parent> implements SystemPropertyConfiguration {


    private SystemPropertyConfigurationImpl(String resourceName, PathAddress pathAddress, SystemPropertyConfiguration.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SystemPropertyConfiguration, SystemPropertyConfiguration.Parent> {
        public Factory(PathAddress pathAddressBase, SystemPropertyConfiguration.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SYSTEM_PROPERTY, parentResource);
        }
        @Override
        public SystemPropertyConfiguration newResourceInstance(String resourceName) {
            return new SystemPropertyConfigurationImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
