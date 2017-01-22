package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.JVM;

/**
 * @author emmartins
 */
public class JvmResourceImpl extends AbstractManageableResource implements JvmResource {

    private JvmResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<JvmResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, JVM, parentResource);
        }
        @Override
        public JvmResource newResourceInstance(String resourceName) {
            return new JvmResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
