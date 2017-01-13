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

import org.jboss.migration.wfly10.config.management.ManageableResource;

import java.util.List;
import java.util.Map;

/**
 * @author emmartins
 */
public abstract class AbstractManageableResourceParent implements ManageableResource.Parent {

    private Map<Class<? extends ManageableResource>, >

    protected abstract <T extends ManageableResource> List<Class<T>> getChildrenResourceTypes(boolean recursive);

    protected interface QueryExecutor

    protected abstract List<ManageableResource> getChildResources();

    @Override
    public List<ManageableResource> getChildResources(boolean recursive) {
        return null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResourcesByType(Class<T> resourceType, boolean recursive) {
        return null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(ManageableResource.Query<T> query) {
        return null;
    }
}
