/*
 * Copyright 2016 Red Hat, Inc.
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
import org.jboss.migration.wfly10.config.management.ProfileResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;

/**
 * @author emmartins
 */
public class ProfileResourceImpl extends AbstractManageableResource<ProfileResource.Parent> implements ProfileResource {

    public static class Factory extends AbstractManageableResource.Factory<ProfileResource, ProfileResource.Parent> {
        public Factory(PathAddress pathAddressBase, ProfileResource.Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, PROFILE, parentResource);
        }
        @Override
        public ProfileResource newResourceInstance(String resourceName) {
            return new ProfileResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }

    private final SubsystemResourceImpl.Factory subsystemResources;

    private ProfileResourceImpl(String resourceName, PathAddress pathAddress, ProfileResource.Parent parent) {
        super(resourceName, pathAddress, parent);
        subsystemResources = new SubsystemResourceImpl.Factory(pathAddress, this);
        addChildResourceFactory(subsystemResources);
    }
}
