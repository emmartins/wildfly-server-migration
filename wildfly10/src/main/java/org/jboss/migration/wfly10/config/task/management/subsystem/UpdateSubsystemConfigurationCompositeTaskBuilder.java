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

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;

/**
 * @author emmartins
 */
public abstract class UpdateSubsystemConfigurationCompositeTaskBuilder<S> extends SubsystemConfigurationLeafTaskBuilder<S> {

    protected UpdateSubsystemConfigurationCompositeTaskBuilder() {
        run((params, taskName) -> context -> {
            final SubsystemConfiguration resource = params.getResource();
            final String configName = resource.getResourceAbsoluteName();
            final ModelNode config = resource.getResourceConfiguration();
            if (config == null) {
                context.getLogger().infof("Skipped subsystem config %s update, not found.", configName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            context.getLogger().debugf("Updating subsystem config %s...", configName);
            final ServerMigrationTaskResult taskResult = updateConfiguration(config, params.getSource(), resource, context, getTaskEnvironment(params, taskName, context));
            context.getLogger().infof("Subsystem config %s updated.", configName);
            return taskResult;
        });
    }

    protected abstract ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemConfiguration subsystemConfiguration, TaskContext taskContext, TaskEnvironment taskEnvironment);
}
