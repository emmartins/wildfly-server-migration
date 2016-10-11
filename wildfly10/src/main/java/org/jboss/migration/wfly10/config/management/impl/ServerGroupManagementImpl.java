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
import org.jboss.as.controller.PathElement;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ServerGroupManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author emmartins
 */
public class ServerGroupManagementImpl implements ServerGroupManagement {

    private final String serverGroupName;
    private final ManageableServerConfiguration configurationManagement;
    private final PathAddress pathAddress;
    private final JVMsManagement JVMsManagement;

    public ServerGroupManagementImpl(String serverGroupName, PathAddress parentPathAddress, ManageableServerConfiguration configurationManagement) {
        this.serverGroupName = serverGroupName;
        this.configurationManagement = configurationManagement;
        final PathElement pathElement = PathElement.pathElement(SERVER_GROUP, serverGroupName);
        this.pathAddress = parentPathAddress != null ? parentPathAddress.append(pathElement) : PathAddress.pathAddress(pathElement);
        this.JVMsManagement = new JVMsManagementImpl(pathAddress, configurationManagement);
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return configurationManagement;
    }

    public JVMsManagement getJVMsManagement() {
        return JVMsManagement;
    }

    @Override
    public String getServerGroupName() {
        return serverGroupName;
    }
}
