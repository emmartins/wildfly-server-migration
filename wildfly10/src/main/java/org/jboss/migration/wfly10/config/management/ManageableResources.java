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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Interface to manage children resources of a specific type
 * @author emmartins
  */
public interface ManageableResources<T extends ManageableResource> {

    T getResource(String resourceName) throws IOException;
    PathAddress getResourcePathAddress(String resourceName);

    Set<String> getResourceNames() throws IOException;
    Set<T> getResources() throws IOException;

    <T extends ManageableResources> List<T> findResources(Class<T> resourcesType) throws IOException;
    <T extends ManageableResource> List<T> findResources(Class<T> resourceType, String resourceName) throws IOException;

    ManageableServerConfiguration getServerConfiguration();
}