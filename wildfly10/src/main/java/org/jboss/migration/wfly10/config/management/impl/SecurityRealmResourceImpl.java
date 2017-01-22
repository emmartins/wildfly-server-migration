package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;

/**
 * @author emmartins
 */
public class SecurityRealmResourceImpl extends AbstractManageableResource implements SecurityRealmResource {


    private SecurityRealmResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SecurityRealmResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SECURITY_REALM, parentResource);
        }
        @Override
        public SecurityRealmResource newResourceInstance(String resourceName) {
            return new SecurityRealmResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
