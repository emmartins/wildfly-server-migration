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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.PathAddress;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public interface SystemPropertyConfiguration extends ManageableResource {

    Type<SystemPropertyConfiguration> RESOURCE_TYPE = new Type<>(SystemPropertyConfiguration.class);

    @Override
    default Type<SystemPropertyConfiguration> getResourceType() {
        return RESOURCE_TYPE;
    }

    interface Parent extends ManageableResource {
        SystemPropertyConfiguration getSystemPropertyConfiguration(String resourceName) throws IOException;
        List<SystemPropertyConfiguration> getSystemPropertyConfigurations() throws IOException;
        Set<String> getSystemPropertyConfigurationNames() throws IOException;
        PathAddress getSystemPropertyConfigurationPathAddress(String resourceName);
        void removeSystemPropertyConfiguration(String resourceName) throws IOException;
    }
}
