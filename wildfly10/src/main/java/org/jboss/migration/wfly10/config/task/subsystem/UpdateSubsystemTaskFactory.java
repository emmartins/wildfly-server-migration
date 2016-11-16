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

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class UpdateSubsystemTaskFactory<S> implements StandaloneServerConfigurationTaskFactory<S>, DomainConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S> {

    private final String name;
    private final String taskName;
    private final String extension;
    protected final List<SubtaskFactory> subsystemMigrationTasks;

    public UpdateSubsystemTaskFactory(Builder builder) {
        this.name = builder.name;
        this.taskName = builder.taskName != null ? builder.taskName : "update-subsystem";
        this.extension = builder.extension;
        this.subsystemMigrationTasks = Collections.unmodifiableList(builder.tasks);
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
        return getTask(SubtaskExecutorAdapters.of(source, configuration, new SubtaskExecutor<S>()));
    }

    @Override
    public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
        return getTask(SubtaskExecutorAdapters.of(source, configuration, new SubtaskExecutor<S>()));
    }

    @Override
    public ServerMigrationTask getTask(S source, StandaloneServerConfiguration configuration) throws Exception {
        return getTask(SubtaskExecutorAdapters.of(source, configuration, new SubtaskExecutor<S>()));
    }

    protected ServerMigrationTask getTask(final ParentServerMigrationTask.SubtaskExecutor subtaskExecutor) throws Exception {
        if (subsystemMigrationTasks == null || subsystemMigrationTasks.isEmpty()) {
            return null;
        }
        final ServerMigrationTaskName serverMigrationTaskName = new ServerMigrationTaskName.Builder(taskName)
                .addAttribute("name", name)
                .build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return serverMigrationTaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (skipExecution(context)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().infof("Updating subsystem %s configurations...", name);
                subtaskExecutor.executeSubtasks(context);
                context.getLogger().infof("Subsystem %s configurations updated.", name);
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    class SubtaskExecutor<S> implements SubsystemsManagementSubtaskExecutor<S> {
        @Override
        public void executeSubtasks(final S source, final SubsystemsManagement subsystemsManagement, final ServerMigrationTaskContext context) throws Exception {
            final String configName = subsystemsManagement.getResourcePathAddress(name).toCLIStyleString();
            final ModelNode subsystemConfig = subsystemsManagement.getResource(name);
            if (subsystemConfig != null) {
                context.getLogger().infof("Updating subsystem %s configuration...", configName);
            }
            for (final SubtaskFactory subsystemMigrationTaskFactory : subsystemMigrationTasks) {
                context.execute(subsystemMigrationTaskFactory.getServerMigrationTask(subsystemConfig, UpdateSubsystemTaskFactory.this, subsystemsManagement));
            }
            if (subsystemConfig != null) {
                context.getLogger().infof("Subsystem %s configuration updated.", configName);
            }
        }
    }

    protected boolean skipExecution(ServerMigrationTaskContext context) {
        return new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemTaskPropertiesPrefix(name)).isSkippedByEnvironment();
    }

    public interface SubtaskFactory {
        /**
         * Retrieves the server migration task's runnable.
         * @param config the subsystem configuration
         * @param parentTask the parent task
         * @param subsystemsManagement the target configuration subsystem management
         * @return
         */
        ServerMigrationTask getServerMigrationTask(ModelNode config, UpdateSubsystemTaskFactory parentTask, SubsystemsManagement subsystemsManagement);
    }

    public static class Builder {

        private final List<SubtaskFactory> tasks = new ArrayList<>();
        private final String extension;
        private final String name;
        private String taskName;

        public Builder(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }

        /**
         * Sets the subsystem migration task name.
         *
         * @param taskName
         * @return
         */
        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        /**
         * Adds a subtask
         *
         * @param subtaskFactory
         * @return
         */
        public Builder subtask(SubtaskFactory subtaskFactory) {
            tasks.add(subtaskFactory);
            return this;
        }

        /**
         * Adds multiple subtasks
         *
         * @param subtaskFactories
         * @return
         */
        public Builder subtasks(SubtaskFactory... subtaskFactories) {
            for (SubtaskFactory subtaskFactory : subtaskFactories) {
                subtask(subtaskFactory);
            }
            return this;
        }

        /**
         * Builds the subsystem.
         *
         * @return
         */
        public UpdateSubsystemTaskFactory build() {
            return new UpdateSubsystemTaskFactory(this);
        }
    }

    public abstract static class Subtask implements ServerMigrationTask {
        private final ModelNode config;
        private final UpdateSubsystemTaskFactory subsystem;
        private final SubsystemsManagement subsystemsManagement;

        protected Subtask(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement) {
            this.config = config;
            this.subsystem = subsystem;
            this.subsystemsManagement = subsystemsManagement;
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem.getName(), this.getName().getName()));
            // check if subtask was skipped by env
            if (taskEnvironment.isSkippedByEnvironment()) {
                return ServerMigrationTaskResult.SKIPPED;
            }
            return run(config, subsystem, subsystemsManagement, context, taskEnvironment);
        }

        protected abstract ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception;
    }
}
