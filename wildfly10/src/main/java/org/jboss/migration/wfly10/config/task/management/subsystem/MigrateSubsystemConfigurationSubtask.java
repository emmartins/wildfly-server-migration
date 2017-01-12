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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A subtask which migrates a legacy subsystem configuration.
 * @author emmartins
 */
public class MigrateSubsystemConfigurationSubtask<S> implements SubsystemConfigurationTask.Subtask<S> {

    @Override
    public ServerMigrationTaskName getName(SubsystemConfigurationTask.Context<S> parentContext) {
        return new ServerMigrationTaskName.Builder("migrate-subsystem-config").addAttribute("name", parentContext.getSubsystemConfigurationName()).build();
    }

    @Override
    public ServerMigrationTaskResult run(SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final String configName = parentContext.getSubsystemConfigurationName();
        final ModelNode config = parentContext.getSubsystemConfiguration();
        if (config == null) {
            taskContext.getLogger().infof("Skipped subsystem config %s update, not found.", configName);
            return ServerMigrationTaskResult.SKIPPED;
        }
        taskContext.getLogger().debugf("Migrating subsystem config %s...", parentContext.getSubsystemConfigurationName());
        final ServerMigrationTaskResult result = migrateConfiguration(parentContext, taskContext, taskEnvironment);
        taskContext.getLogger().infof("Subsystem config %s migrated.", configName);
        return result;
    }

    protected ServerMigrationTaskResult migrateConfiguration(SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final ModelNode op = Util.createEmptyOperation("migrate", parentContext.getSubsystemConfigurationPathAddress());
        final ModelNode result = parentContext.getServerConfiguration().getModelControllerClient().execute(op);
        taskContext.getLogger().debugf("Op result: %s", result.asString());
        final String outcome = result.get(OUTCOME).asString();
        final String configName = parentContext.getSubsystemConfigurationName();
        if(!SUCCESS.equals(outcome)) {
            throw new RuntimeException("Subsystem config "+configName+" migration failed: "+result.get("migration-error").asString());
        } else {
            final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().sucess();
            final List<String> migrateWarnings = new ArrayList<>();
            if (result.get(RESULT).hasDefined("migration-warnings")) {
                for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                    migrateWarnings.add(modelNode.asString());
                }
            }
            processWarnings(migrateWarnings, parentContext, taskContext, taskEnvironment);
            if (migrateWarnings.isEmpty()) {
                taskContext.getLogger().infof("Subsystem config %s migrated.", configName);
            } else {
                taskContext.getLogger().infof("Subsystem config %s migrated with warnings: %s", configName, migrateWarnings);
                resultBuilder.addAttribute("migration-warnings", migrateWarnings);
            }
            // FIXME tmp workaround for legacy subsystems which do not remove itself
            if (parentContext.getSubsystemConfiguration() != null) {
                // remove itself after migration
                parentContext.removeSubsystemConfiguration();
                taskContext.getLogger().debugf("Subsystem config %s removed after migration.", configName);
            }
            return resultBuilder.build();
        }
    }

    protected void processWarnings(List<String> migrateWarnings, SubsystemConfigurationTask.Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) {
    }
}
