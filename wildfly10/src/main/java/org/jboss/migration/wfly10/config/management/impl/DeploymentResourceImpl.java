package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;

/**
 * @author emmartins
 */
public class DeploymentResourceImpl extends ManageableResourceImpl implements DeploymentResource {

    public static final ManageableResourceImpl.Type TYPE = new ManageableResourceImpl.Type<>(DeploymentResource.class);

    private DeploymentResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, parent, serverConfiguration);
    }

    public static class Factory extends ManageableResourceImpl.Factory<DeploymentResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource, ManageableServerConfiguration serverConfiguration) {
            super(TYPE, pathAddressBase, DEPLOYMENT, parentResource, serverConfiguration);
        }
        @Override
        public DeploymentResource newResourceInstance(String resourceName) {
            return new DeploymentResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource, serverConfiguration);
        }
    }
}
