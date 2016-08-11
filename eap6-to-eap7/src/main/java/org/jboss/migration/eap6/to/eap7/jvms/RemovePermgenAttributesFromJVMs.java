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

package org.jboss.migration.eap6.to.eap7.jvms;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.task.JVMsMigration;

/**
 * Removes permgen from JVM Configs.
 * @author emmartins
 */
public class RemovePermgenAttributesFromJVMs implements JVMsMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "remove-permgen";

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, JVMsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, final ServerPath<EAP6Server> source, final JVMsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).addAttribute("name", resourceName).build();
        final ServerMigrationTask subtask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final ModelNode config = resourceManagement.getResource(resourceName);
                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(resourceName);
                boolean updated = false;
                if (config.hasDefined("permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "permgen-size");
                    resourceManagement.getServerConfiguration().executeManagementOperation(op);
                    updated = true;
                }
                if (config.hasDefined("max-permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "max-permgen-size");
                    resourceManagement.getServerConfiguration().executeManagementOperation(op);
                    updated = true;
                }
                if (!updated) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().infof("Permgen removed from JVM %s", resourceName);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        subtasks.add(subtask);
    }

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of jvm config related env properties
         */
        String JVM_PROPERTIES_PREFIX = "jvm.";
        /**
         * the prefix for the name of the task related env properties
         */
        String PROPERTIES_PREFIX = JVM_PROPERTIES_PREFIX + SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips the task execution
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }
}