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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class ProfileManagementImpl extends ManageableResourceImpl implements ProfileManagement {

    private final SubsystemsManagement subsystemsManagement;

    public ProfileManagementImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, serverConfiguration);
        this.subsystemsManagement = new SubsystemsManagementImpl(pathAddress, serverConfiguration);
    }

    public SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }

    @Override
    public String getProfileName() {
        return getResourceName();
    }

    @Override
    public List findResources(Class resourcesType) throws IOException {
        if (resourcesType.isAssignableFrom(SubsystemsManagement.class)) {
            return Collections.singletonList(getSubsystemsManagement());
        } else {
            return Collections.emptyList();
        }
    }
}
