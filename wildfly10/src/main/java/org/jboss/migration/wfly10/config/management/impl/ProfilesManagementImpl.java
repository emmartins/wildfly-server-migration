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
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;

/**
 * @author emmartins
 */
public class ProfilesManagementImpl extends ManageableResourcesImpl implements ProfilesManagement {

    public ProfilesManagementImpl(PathAddress parentPathAddress, ManageableServerConfiguration configuration) {
        super(PROFILE, parentPathAddress, configuration);
    }

    @Override
    public ProfileManagement getProfileManagement(String profileName) {
        return new ProfileManagementImpl(profileName, parentPathAddress, getServerConfiguration());
    }

    @Override
    public List findResources(Class resourcesType) throws IOException {
        if (resourcesType.isAssignableFrom(SubsystemsManagement.class)) {
            final Set<String> resourceNames = getResourceNames();
            if (resourceNames.isEmpty()) {
                return Collections.emptyList();
            } else {
                final List<SubsystemsManagement> result = new ArrayList<>();
                for (String resourceName : resourceNames) {
                    result.add(getProfileManagement(resourceName).getSubsystemsManagement());
                }
                return result;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List findResources(Class resourceType, String resourceName) throws IOException {
        if (resourceType.isAssignableFrom(ProfileManagement.class) && getResourceNames().contains(resourceName)) {
            return Collections.singletonList(getProfileManagement(resourceName));
        } else {
            return Collections.emptyList();
        }
    }
}