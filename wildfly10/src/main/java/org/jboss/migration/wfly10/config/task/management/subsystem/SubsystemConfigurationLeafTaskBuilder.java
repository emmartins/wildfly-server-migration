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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component2.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

/**
 * @author emmartins
 */
public abstract class SubsystemConfigurationLeafTaskBuilder<S> extends ManageableResourceLeafTask.Builder<S, SubsystemConfiguration> {

    protected SubsystemConfigurationLeafTaskBuilder() {
        skipPolicy(TaskSkipPolicy.Builders.skipByTaskEnvironment()(params, taskName) ->context -> getTaskEnvironment(params, taskName, context).isSkippedByEnvironment());
    }

    protected TaskEnvironment getTaskEnvironment(ManageableResourceBuildParameters<S, SubsystemConfiguration> buildParameters, ServerMigrationTaskName taskName, TaskContext taskContext) {
        return new TaskEnvironment(taskContext.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(buildParameters.getResource().getResourceName(), taskName.getName()));
    }
}
