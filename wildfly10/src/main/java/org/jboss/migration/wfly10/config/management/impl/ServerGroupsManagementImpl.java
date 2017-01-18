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
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResources;
import org.jboss.migration.wfly10.config.management.ServerGroupManagement;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author emmartins
 */
public class ServerGroupsManagementImpl extends ManageableResourcesImpl implements ServerGroupsManagement {

    public ServerGroupsManagementImpl(PathAddress parentPathAddress, HostControllerConfiguration configurationManagement) {
        super(SERVER_GROUP, parentPathAddress, configurationManagement);
    }

    @Override
    public ServerGroupManagement getServerGroupManagement(String serverGroup) {
        return new ServerGroupManagementImpl(serverGroup, getResourcePathAddress(serverGroup), getServerConfiguration());
    }

    @Override
    public List findResources(Class resourcesType) throws IOException {
        if (resourcesType.isAssignableFrom(JVMsManagement.class)) {
            final Set<String> serverGroupNames = getResourceNames();
            if (serverGroupNames.isEmpty()) {
                return Collections.emptyList();
            } else {
                final List<JVMsManagement> result = new ArrayList<>();
                for (String serverGroupName : serverGroupNames) {
                    result.add(getServerGroupManagement(serverGroupName).getJVMsManagement());
                }
                return result;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List findResources(Class resourceType, String resourceName) throws IOException {
        if (resourceType.isAssignableFrom(ServerGroupManagement.class) && getResourceNames().contains(resourceName)) {
            return Collections.singletonList(getServerGroupManagement(resourceName));
        } else {
            return Collections.emptyList();
        }
    }
}