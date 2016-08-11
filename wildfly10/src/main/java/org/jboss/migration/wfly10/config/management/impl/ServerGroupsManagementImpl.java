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
import org.jboss.migration.wfly10.config.management.ServerGroupManagement;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author emmartins
 */
public class ServerGroupsManagementImpl extends ResourcesManagementImpl implements ServerGroupsManagement {

    public ServerGroupsManagementImpl(PathAddress parentPathAddress, HostControllerConfiguration configurationManagement) {
        super(SERVER_GROUP, parentPathAddress, configurationManagement);
    }

    @Override
    public ServerGroupManagement getServerGroupManagement(String serverGroup) {
        return new ServerGroupManagementImpl(serverGroup, getParentPathAddress(), getServerConfiguration());
    }
}