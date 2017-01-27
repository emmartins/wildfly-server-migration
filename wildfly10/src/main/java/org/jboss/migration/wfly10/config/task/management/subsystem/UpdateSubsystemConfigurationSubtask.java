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
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

/**
 * @author emmartins
 */
public abstract class UpdateSubsystemConfigurationSubtask<S> implements SubsystemConfigurationTask.SubtaskFactory<S> {

    public abstract ServerMigrationTaskName getName(S source, SubsystemConfiguration subsystemConfiguration, TaskContext parentContext);

    @Override
    public ServerMigrationTask getTask(S source, SubsystemConfiguration resource, TaskContext context) throws Exception {
        final ServerMigrationTaskName taskName = getName(source, resource, context);
        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(resource.getResourceName(), taskName.getName()));
        final SubsystemConfigurationLeafTask.Runnable<S> runnable = (source1, resource1, context1) -> {
            final String configName = resource1.getResourceAbsoluteName();
            final ModelNode config = resource1.getResourceConfiguration();
            if (config == null) {
                context1.getLogger().infof("Skipped subsystem config %s update, not found.", configName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            context1.getLogger().debugf("Updating subsystem config %s...", configName);
            final ServerMigrationTaskResult taskResult = updateConfiguration(config, source1, resource1, context1, taskEnvironment);
            context1.getLogger().infof("Subsystem config %s updated.", configName);
            return taskResult;
        };
        return new SubsystemConfigurationLeafTask.Builder<>(taskName, runnable)
                .skipper(context1 -> taskEnvironment.isSkippedByEnvironment())
                .build(source, resource);
    }

    protected abstract ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemConfiguration subsystemConfiguration, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception;
}
