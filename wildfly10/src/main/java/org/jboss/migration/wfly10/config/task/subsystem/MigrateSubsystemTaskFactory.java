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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationSubtasks;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

/**
 * @author emmartins
 */
public class MigrateSubsystemTaskFactory<S> implements StandaloneServerConfigurationTaskFactory<S>, DomainConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S> {

    private final String subsystemName;
    private final String taskNameName;
    private final String extensionModule;

    public MigrateSubsystemTaskFactory(String subsystemName) {
        this(subsystemName, null);
    }

    public MigrateSubsystemTaskFactory(String subsystemName, String extensionModule) {
        this.subsystemName = subsystemName;
        this.taskNameName = "migrate-subsystem";
        this.extensionModule = extensionModule;
    }

    @Override
    public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
        return getTask(source, configuration, SubtaskExecutorAdapters.of(source, configuration, new ConfigMigration<S>(subsystemName)));
    }

    @Override
    public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
        return getTask(source, configuration, SubtaskExecutorAdapters.of(source, configuration, new ConfigMigration<S>(subsystemName)));
    }

    @Override
    public ServerMigrationTask getTask(S source, StandaloneServerConfiguration configuration) throws Exception {
        return getTask(source, configuration, SubtaskExecutorAdapters.of(source, configuration, new ConfigMigration<S>(subsystemName)));
    }

    protected ServerMigrationTask getTask(final S source, final ManageableServerConfiguration configuration, ParentServerMigrationTask.SubtaskExecutor subtaskExecutor) throws Exception {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder(taskNameName)
                .addAttribute("name", subsystemName)
                .build();
        final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(taskName)
                .subtask(subtaskExecutor)
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {
                        context.getLogger().infof("Migrating subsystem %s configurations...", subsystemName);
                    }
                    @Override
                    public void done(TaskContext context) {
                        context.getLogger().infof("Subsystem %s configurations migrated.", subsystemName);
                    }
                });
        if (extensionModule != null) {
            taskBuilder.subtask(SubtaskExecutorAdapters.of(source, configuration, new RemoveExtension<S>(extensionModule)));
        }
        return  taskBuilder.build();
    }

    protected static class ConfigMigration<S> implements SubsystemsConfigurationSubtasks<S> {

        private final String subsystemName;

        protected ConfigMigration(String subsystemName) {
            this.subsystemName = subsystemName;
        }

        @Override
        public void executeSubtasks(S source, final SubsystemsManagement subsystemsManagement, TaskContext context) throws Exception {
            final String configName = subsystemsManagement.getResourcePathAddress(subsystemName).toCLIStyleString();
            final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("migrate-config")
                    .addAttribute("name", configName)
                    .build();
            final ServerMigrationTask task = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return taskName;
                }
                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    final ModelNode subsystemConfig = subsystemsManagement.getResource(subsystemName);
                    if (subsystemConfig == null) {
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    context.getLogger().debugf("Migrating subsystem %s config %s...", subsystemName, configName);
                    final PathAddress address = subsystemsManagement.getResourcePathAddress(subsystemName);
                    final ModelNode op = Util.createEmptyOperation("migrate", address);
                    final ModelNode result = subsystemsManagement.getServerConfiguration().getModelControllerClient().execute(op);
                    context.getLogger().debugf("Op result: %s", result.asString());
                    final String outcome = result.get(OUTCOME).asString();
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
                        processWarnings(migrateWarnings, subsystemsManagement, context);
                        if (migrateWarnings.isEmpty()) {
                            context.getLogger().infof("Subsystem config %s migrated.", configName);
                        } else {
                            context.getLogger().infof("Subsystem config %s migrated with warnings: %s", configName, migrateWarnings);
                            resultBuilder.addAttribute("migration-warnings", migrateWarnings);
                        }
                        // FIXME tmp workaround for legacy subsystems which do not remove itself
                        if (subsystemsManagement.getResourceNames().contains(subsystemName)) {
                            // remove itself after migration
                            subsystemsManagement.removeResource(subsystemName);
                            context.getLogger().debugf("Subsystem config %s removed after migration.", configName);
                        }
                        return resultBuilder.build();
                    }
                }
            };
            context.execute(task);
        }

        /**
         * Post migration processing.
         * @param migrationWarnings the warnings that resulted from doing the migration
         * @param subsystemsManagement the subsystem management
         * @param context the task context
         * @throws Exception if there was a failure processing the warnings
         */
        protected void processWarnings(List<String> migrationWarnings, SubsystemsManagement subsystemsManagement, TaskContext context) throws Exception {
        }
    }
}
