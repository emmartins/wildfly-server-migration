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

package org.jboss.migration.wfly10.config.task.management.resource;

import org.jboss.migration.core.task.component2.BuildParameters;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.ServerConfigurationBuildParameters;

import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public interface ResourcesCollection<S, R extends ManageableResource> extends ServerConfigurationBuildParameters<S> {

    Collection<? extends R> getResources();

    static <S, T extends ManageableResource, R extends ManageableResource> BuildParameters.Mapper<ResourcesCollection<S, T>, ResourcesCollection<S, R>> from(ManageableResourceSelector<R> rSelector, ResourcesCollection<S, T> tParameters) {
        return stManageableResourcesCollection -> {
            final S rSource = stManageableResourcesCollection.getSource();
            final ManageableServerConfiguration rServerConfiguration = stManageableResourcesCollection.getServerConfiguration();
            final Collection<R> rCollection = rSelector.fromResources(tParameters.getResources());
            return Collections.singleton(new ResourcesCollection<S, R>() {
                @Override
                public S getSource() {
                    return rSource;
                }

                @Override
                public ManageableServerConfiguration getServerConfiguration() {
                    return rServerConfiguration;
                }

                @Override
                public Collection<? extends R> getResources() {
                    return rCollection;
                }
            });
        };
    }
}