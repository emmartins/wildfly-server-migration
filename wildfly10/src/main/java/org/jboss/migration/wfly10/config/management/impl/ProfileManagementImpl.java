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
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;

/**
 * @author emmartins
 */
public class ProfileManagementImpl implements ProfileManagement {

    private final String profileName;
    private final ManageableServerConfiguration configuration;
    private final PathAddress pathAddress;
    private final SubsystemsManagement subsystemsManagement;

    public ProfileManagementImpl(String profileName, PathAddress parentPathAddress, ManageableServerConfiguration configuration) {
        this.profileName = profileName;
        this.configuration = configuration;
        final PathElement pathElement = PathElement.pathElement(PROFILE, profileName);
        this.pathAddress = parentPathAddress != null ? parentPathAddress.append(pathElement) : PathAddress.pathAddress(pathElement);
        this.subsystemsManagement = new SubsystemsManagementImpl(pathAddress, configuration);
    }

    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return configuration;
    }

    @Override
    public String getProfileName() {
        return profileName;
    }
}
