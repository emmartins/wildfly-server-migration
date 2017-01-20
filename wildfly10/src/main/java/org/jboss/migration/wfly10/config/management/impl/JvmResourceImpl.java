package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.JVM;

/**
 * @author emmartins
 */
public class JvmResourceImpl extends ManageableResourceImpl implements JvmResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(JvmResource.class);

    private JvmResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<JvmResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, JVM, parentResource, serverConfiguration);
        }
        @Override
        public JvmResource newResourceInstance(String resourceName) {
            return new JvmResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
