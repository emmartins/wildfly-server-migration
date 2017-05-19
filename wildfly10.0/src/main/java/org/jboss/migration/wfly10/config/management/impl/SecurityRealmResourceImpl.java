package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;

/**
 * @author emmartins
 */
public class SecurityRealmResourceImpl extends AbstractManageableResource<SecurityRealmResource.Parent> implements SecurityRealmResource {


    private SecurityRealmResourceImpl(String resourceName, PathAddress pathAddress, SecurityRealmResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SecurityRealmResource, SecurityRealmResource.Parent> {
        public Factory(PathAddress pathAddressBase, SecurityRealmResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SECURITY_REALM, parentResource);
        }
        @Override
        public SecurityRealmResource newResourceInstance(String resourceName) {
            return new SecurityRealmResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
