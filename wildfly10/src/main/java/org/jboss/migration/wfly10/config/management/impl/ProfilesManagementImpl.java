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
import org.jboss.migration.wfly10.config.management.ChildResources;
import org.jboss.migration.wfly10.config.management.ManageableNode;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileManagement;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;

/**
 * @author emmartins
 */
public class ProfilesManagementImpl extends ResourcesManagementImpl implements ProfilesManagement {

    public ProfilesManagementImpl(PathAddress parentPathAddress, ManageableServerConfiguration configuration) {
        super(PROFILE, parentPathAddress, configuration);
    }

    @Override
    public ProfileManagement getProfileManagement(String profileName) {
        return new ProfileManagementImpl(profileName, parentPathAddress, getServerConfiguration());
    }

    @Override
    public Class getType() {
        return ProfilesManagement.class;
    }

    @Override
    public List findChildren(Select select) {
        select.getType()
        return null;
    }

    @Override
    public List findChildren(ManageableNode.Query query) {
        return null;
    }
}