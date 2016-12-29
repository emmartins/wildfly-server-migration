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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesComponentTaskBuilder;

import java.util.Collections;

/**
 * @author emmartins
 */
public interface ManageableServerConfigurationTaskFactory<S, T extends ManageableServerConfiguration> {

    ServerMigrationTask getTask(S source, T configuration);

    static <S, T extends ManageableServerConfiguration> ManageableServerConfigurationTaskFactory<S, T> of(ManageableServerConfigurationComponentTaskBuilder<S, ?> subtaskBuilder) {
        return (source, configuration) -> subtaskBuilder.build(new ManageableServerConfigurationBuildParametersImpl<>(source, configuration));
    }

    static <S, T extends ManageableServerConfiguration> ManageableServerConfigurationTaskFactory<S, T> of(ManageableResourceComponentTaskBuilder<S, ManageableResource, ?> subtaskBuilder) {
        return (source, configuration) -> subtaskBuilder.build(new ManageableResourceBuildParametersImpl<>(source, configuration));
    }

    static <S, T extends ManageableServerConfiguration> ManageableServerConfigurationTaskFactory<S, T> of(ManageableResourcesComponentTaskBuilder<S, ManageableResource, ?> subtaskBuilder) {
        return (source, configuration) -> subtaskBuilder.build(new ManageableResourcesBuildParametersImpl<>(source, configuration, Collections.singleton(configuration)));
    }
}