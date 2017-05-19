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
import org.jboss.migration.wfly10.config.management.PathResource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;

/**
 * @author emmartins
 */
public class PathResourceImpl extends AbstractManageableResource<PathResource.Parent> implements PathResource {

    private PathResourceImpl(String resourceName, PathAddress pathAddress, Parent parent) {
        super(resourceName, pathAddress, parent);
    }

    public static class Factory extends AbstractManageableResource.Factory<PathResource, Parent> {
        public Factory(PathAddress pathAddressBase, Parent parentResource) {
            super(RESOURCE_TYPE, pathAddressBase, PATH, parentResource);
        }
        @Override
        public PathResource newResourceInstance(String resourceName) {
            return new PathResourceImpl(resourceName, getResourcePathAddress(resourceName), parentResource);
        }
    }
}
