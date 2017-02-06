package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.DeploymentResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;

/**
 * @author emmartins
 */
public class DeploymentResourceImpl extends AbstractManageableResource<DeploymentResource.Parent> implements DeploymentResource {

    private DeploymentResourceImpl(String resourceName, PathAddress pathAddress, DeploymentResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<DeploymentResource, DeploymentResource.Parent> {
        public Factory(PathAddress pathAddressBase, DeploymentResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, DEPLOYMENT, parentResource);
        }
        @Override
        public DeploymentResource newResourceInstance(String resourceName) {
            return new DeploymentResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
