package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.SubsystemResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author emmartins
 */
public class SubsystemResourceImpl extends AbstractManageableResource<SubsystemResource.Parent> implements SubsystemResource {

    private SubsystemResourceImpl(String resourceName, PathAddress pathAddress, SubsystemResource.Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<SubsystemResource, SubsystemResource.Parent> {
        public Factory(PathAddress pathAddressBase, SubsystemResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, SUBSYSTEM, parentResource);
        }
        @Override
        public SubsystemResource newResourceInstance(String resourceName) {
            return new SubsystemResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
