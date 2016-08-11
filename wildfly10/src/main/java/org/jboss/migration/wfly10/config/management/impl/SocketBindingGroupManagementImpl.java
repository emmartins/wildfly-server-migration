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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;

/**
 * @author emmartins
 */
public class SocketBindingGroupManagementImpl implements SocketBindingGroupManagement {

    private final String socketBindingGroupName;
    private final ManageableServerConfiguration serverConfiguration;
    private final PathAddress pathAddress;
    private final SocketBindingsManagement socketBindingsManagement;

    public SocketBindingGroupManagementImpl(String socketBindingGroupName, PathAddress parentPathAddress, ManageableServerConfiguration serverConfiguration) {
        this.socketBindingGroupName = socketBindingGroupName;
        this.serverConfiguration = serverConfiguration;
        final PathElement pathElement = PathElement.pathElement(SOCKET_BINDING_GROUP, socketBindingGroupName);
        this.pathAddress = parentPathAddress != null ? parentPathAddress.append(pathElement) : PathAddress.pathAddress(pathElement);
        this.socketBindingsManagement = new SocketBindingsManagementImpl(pathAddress, serverConfiguration);
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public String getSocketBindingGroupName() {
        return socketBindingGroupName;
    }

    @Override
    public SocketBindingsManagement getSocketBindingsManagement() {
        return socketBindingsManagement;
    }
}
