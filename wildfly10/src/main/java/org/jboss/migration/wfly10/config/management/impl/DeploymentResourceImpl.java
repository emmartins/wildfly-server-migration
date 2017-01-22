package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;

/**
 * @author emmartins
 */
public class DeploymentResourceImpl extends AbstractManageableResource implements DeploymentResource {

    private DeploymentResourceImpl(String resourceName, PathAddress pathAddress, ManageableResource parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<DeploymentResource> {
        public Factory(PathAddress pathAddressBase, ManageableResource parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, DEPLOYMENT, parentResource);
        }
        @Override
        public DeploymentResource newResourceInstance(String resourceName) {
            return new DeploymentResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
