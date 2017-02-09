/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.DeploymentOverlayResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT_OVERLAY;

/**
 * @author emmartins
 */
public class DeploymentOverlayResourceImpl extends AbstractManageableResource<DeploymentOverlayResource.Parent> implements DeploymentOverlayResource {

    private DeploymentOverlayResourceImpl(String resourceName, PathAddress pathAddress, Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<DeploymentOverlayResource, Parent> {
        public Factory(PathAddress pathAddressBase, Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, DEPLOYMENT_OVERLAY, parentResource);
        }
        @Override
        public DeploymentOverlayResource newResourceInstance(String resourceName) {
            return new DeploymentOverlayResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
