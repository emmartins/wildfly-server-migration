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

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A subtask which migrates a legacy subsystem configuration.
 * @author emmartins
 */
public class MigrateSubsystemConfigurationSubtask<S> implements SubsystemConfigurationTask.SubtaskFactory<S> {

    @Override
    public ServerMigrationTask getTask(S source, SubsystemConfiguration resource, TaskContext context) throws Exception {
        final ServerMigrationTaskName taskName = getName(source, resource, context);
        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(resource.getResourceName(), taskName.getName()));
        final SubsystemConfigurationLeafTask.Runnable<S> runnable = (source1, resource1, context1) -> {
            final String configName = resource1.getResourceAbsoluteName();
            final ModelNode config = resource1.getResourceConfiguration();
            if (config == null) {
                context1.getLogger().infof("Skipped subsystem config %s migration, not found.", configName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            context1.getLogger().debugf("Migrating subsystem config %s...", configName);
            final ServerMigrationTaskResult result = migrateConfiguration(resource1, context1, taskEnvironment);
            context1.getLogger().infof("Subsystem config %s migrated.", configName);
            return result;
        };
        return new SubsystemConfigurationLeafTask.Builder<>(taskName, runnable)
                .skipper(context1 -> taskEnvironment.isSkippedByEnvironment())
                .build(source, resource);
    }

    public ServerMigrationTaskName getName(S source, SubsystemConfiguration subsystemConfiguration, TaskContext parentContext) {
        return new ServerMigrationTaskName.Builder("migrate-subsystem-config").addAttribute("name", subsystemConfiguration.getResourceAbsoluteName()).build();
    }

    protected ServerMigrationTaskResult migrateConfiguration(SubsystemConfiguration subsystemConfiguration, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final ModelNode op = Util.createEmptyOperation("migrate", subsystemConfiguration.getResourcePathAddress());
        final ModelNode result = subsystemConfiguration.getServerConfiguration().getModelControllerClient().execute(op);
        taskContext.getLogger().debugf("Op result: %s", result.asString());
        final String outcome = result.get(OUTCOME).asString();
        final String configName = subsystemConfiguration.getResourceAbsoluteName();
        if(!SUCCESS.equals(outcome)) {
            throw new RuntimeException("Subsystem config "+configName+" migration failed: "+result.get("migration-error").asString());
        } else {
            final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().success();
            final List<String> migrateWarnings = new ArrayList<>();
            if (result.get(RESULT).hasDefined("migration-warnings")) {
                for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                    migrateWarnings.add(modelNode.asString());
                }
            }
            processWarnings(migrateWarnings, subsystemConfiguration, taskContext, taskEnvironment);
            if (migrateWarnings.isEmpty()) {
                taskContext.getLogger().infof("Subsystem config %s migrated.", configName);
            } else {
                taskContext.getLogger().infof("Subsystem config %s migrated with warnings: %s", configName, migrateWarnings);
                resultBuilder.addAttribute("migration-warnings", migrateWarnings);
            }
            // FIXME tmp workaround for legacy subsystems which do not remove itself
            if (subsystemConfiguration.getResourceConfiguration() != null) {
                // remove itself after migration
                subsystemConfiguration.getParentResource().removeResource(SubsystemConfiguration.RESOURCE_TYPE, subsystemConfiguration.getResourceName());
                taskContext.getLogger().debugf("Subsystem config %s removed after migration.", configName);
            }
            return resultBuilder.build();
        }
    }

    protected void processWarnings(List<String> migrateWarnings, SubsystemConfiguration subsystemConfiguration, TaskContext taskContext, TaskEnvironment taskEnvironment) {
    }
}
