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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.executor.SubsystemsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemConfigurationSubtask;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemConfigurationSubtask;

/**
 * @author emmartins
 */
public class SubsystemConfigurationTaskBuilders<S> {

    private SubsystemConfigurationTaskBuilders() {
    }

    public static <S> SubsystemsManagementTask.Builder<S> addSubsystem(String extension, String subsystem) {
        return addSubsystem(extension, subsystem, new AddSubsystemConfigurationSubtask());

    }

    public static <S> SubsystemsManagementTask.Builder<S> addSubsystem(final String extension, final String subsystem, final AddSubsystemConfigurationSubtask subsystemConfigSubtask) {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystem).build();
        final AbstractServerMigrationTask.Listener listener = new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding subsystem %s...", subsystem);
            }
            @Override
            public void done(TaskContext context) {
                if (context.hasSucessfulSubtasks()) {
                    context.getLogger().infof("Subsystem %s added.", subsystem);
                } else {
                    context.getLogger().infof("Subsystem %s not added.", subsystem);
                }
            }
        };
        return new SubsystemsManagementTask.Builder<S>(taskName).listener(listener).subtask(getSubtaskExecutor(extension, subsystem, subsystemConfigSubtask));
    }

    public static <S> SubsystemsManagementTask.Builder<S> updateSubsystem(final String extension, final String subsystem, final AddSubsystemConfigurationSubtask subsystemConfigSubtask) {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystem).build();
        final AbstractServerMigrationTask.Listener listener = new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding subsystem %s...", subsystem);
            }
            @Override
            public void done(TaskContext context) {
                if (context.hasSucessfulSubtasks()) {
                    context.getLogger().infof("Subsystem %s added.", subsystem);
                } else {
                    context.getLogger().infof("Subsystem %s not added.", subsystem);
                }
            }
        };
        return new SubsystemsManagementTask.Builder<S>(taskName).listener(listener).subtask(getSubtaskExecutor(extension, subsystem, subsystemConfigSubtask));
    }

    private static <S> SubsystemsManagementSubtaskExecutor<S> getSubtaskExecutor(final String extension, final String subsystem, final SubsystemConfigurationSubtask<S> subtask) {
        return new SubsystemsManagementSubtaskExecutor<S>() {
            @Override
            public void executeSubtasks(S source, SubsystemsManagement resourceManagement, TaskContext context) throws Exception {
                final SubsystemConfigurationSubtask.ParentTaskContext<S> parentTaskContext = getParentTaskContext(extension, subsystem, source, resourceManagement, context);
                final ServerMigrationTaskName subtaskName = subtask.getName(parentTaskContext);
                if (subtaskName != null) {
                    final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem, subtaskName.getName()));
                    final AbstractServerMigrationTask.Builder builder = new AbstractServerMigrationTask.Builder(subtaskName)
                            .skipper(new AbstractServerMigrationTask.Skipper() {
                                @Override
                                public boolean isSkipped(TaskContext context) {
                                    return taskEnvironment.isSkippedByEnvironment();
                                }
                            });
                    final ServerMigrationTask task = new AbstractServerMigrationTask(builder) {
                        @Override
                        protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
                            return subtask.run(parentTaskContext, taskContext, taskEnvironment);
                        }
                    };
                    context.execute(task);
                }
            }
        };
    }

    private static <S> SubsystemConfigurationSubtask.ParentTaskContext getParentTaskContext(final String extension, final String subsystem, final S source, final SubsystemsManagement subsystemsManagement, final TaskContext taskContext) {
        return new SubsystemConfigurationSubtask.ParentTaskContext<S>() {
            @Override
            public String getExtension() {
                return extension;
            }
            @Override
            public String getSubsystem() {
                return subsystem;
            }
            @Override
            public S getSource() {
                return source;
            }
            @Override
            public SubsystemsManagement getSubsystemsManagement() {
                return subsystemsManagement;
            }
            @Override
            public String getConfigName() {
                return subsystemsManagement.getResourcePathAddress(subsystem).toCLIStyleString();
            }
        };
    }
}
