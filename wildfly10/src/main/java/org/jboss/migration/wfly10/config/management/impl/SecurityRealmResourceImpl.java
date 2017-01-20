package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SecurityRealmResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;

/**
 * @author emmartins
 */
public class SecurityRealmResourceImpl extends ManageableResourceImpl implements SecurityRealmResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(SecurityRealmResource.class);

    private SecurityRealmResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<SecurityRealmResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, SECURITY_REALM, parentResource, serverConfiguration);
        }
        @Override
        public SecurityRealmResource newResourceInstance(String resourceName) {
            return new SecurityRealmResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
