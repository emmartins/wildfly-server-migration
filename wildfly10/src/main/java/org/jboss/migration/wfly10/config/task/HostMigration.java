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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostResources;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostConfiguration;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostsManagementTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class HostMigration<S> implements HostsManagementTaskFactory<S> {

    public static final String HOSTS = "hosts";
    public static final String HOST = "host";

    public static final String MIGRATION_REPORT_TASK_ATTR_NAME = "name";

    protected final HostConfigurationProvider hostConfigurationProvider;
    protected final List<HostConfigurationTaskFactory<S>> subtaskFactories;

    protected HostMigration(Builder builder) {
        this.hostConfigurationProvider = builder.hostConfigurationProvider;
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    protected ServerMigrationTask getResourceSubtask(final String resourceName, final S source, final HostResources resourceManagement) throws Exception {
        final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder(HOST).addAttribute(MIGRATION_REPORT_TASK_ATTR_NAME, resourceName).build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return subtaskName;
            }

            @Override
            public ServerMigrationTaskResult run(final TaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Migrating host %s in host configuration %s ...", resourceName, source);
                final HostConfiguration hostConfiguration = hostConfigurationProvider.getHostConfiguration(resourceName, resourceManagement.getHostControllerConfiguration());
                hostConfiguration.start();
                try {
                    for (HostConfigurationTaskFactory<S> subtaskFactory : subtaskFactories) {
                        final ServerMigrationTask subtask = subtaskFactory.getTask(source, hostConfiguration);
                        if (subtask != null) {
                            context.execute(subtask);
                        }
                    }
                } finally {
                    hostConfiguration.stop();
                    context.getLogger().infof("Migration of host %s in host configuration %s done.", resourceName, source);
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    @Override
    public ServerMigrationTask getTask(final S source, final HostResources hostResources) throws Exception {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder(HOSTS).build();
        return new ParentServerMigrationTask.Builder(taskName)
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {
                        context.getLogger().infof("Hosts migration starting...");
                    }
                    @Override
                    public void done(TaskContext context) {
                        context.getLogger().infof("Hosts migration done.");
                    }
                })
                .subtask(new ParentServerMigrationTask.SubtaskExecutor() {
                    @Override
                    public void executeSubtasks(TaskContext context) throws Exception {
                        for (String resourceName : hostResources.getResourceNames()) {
                            final ServerMigrationTask subtask = getResourceSubtask(resourceName, source, hostResources);
                            if (subtask != null) {
                                context.execute(subtask);
                            }
                        }
                    }
                })
                .build();
    }

    /**
     * Provider for the host manageable configuration
     */
    public interface HostConfigurationProvider {
        HostConfiguration getHostConfiguration(String host, HostControllerConfiguration hostController) throws Exception;
    }

    /**
     * The builder.
     * @param <S> the source for the configuration
     */
    public static class Builder<S> {

        private HostConfigurationProvider hostConfigurationProvider;
        private final List<HostConfigurationTaskFactory<S>> subtaskFactories;

        public Builder() {
            this(new EmbeddedHostConfiguration.HostConfigFileMigrationFactory());
        }

        public Builder(HostConfigurationProvider hostConfigurationProvider) {
            this.hostConfigurationProvider = hostConfigurationProvider;
            subtaskFactories = new ArrayList<>();
        }

        public Builder<S> subtask(HostConfigurationTaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public Builder<S> subtask(final ManageableServerConfigurationTaskFactory<S, HostConfiguration> subtaskFactory) {
            return subtask(new HostConfigurationTaskFactory<S>() {
                @Override
                public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
                    return subtaskFactory.getTask(source, configuration);
                }
            });
        }

        /*
        public Builder<S> subsystems(final SubsystemsMigration<S> resourcesMigration) {
            return subtask(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getSubsystemsManagementTask(source, configuration.getSubsystemsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> securityRealms(final SecurityRealmsMigration<S> resourcesMigration) {
            return subtask(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getSecurityRealmsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> securityRealms(final SecurityRealmsMigration.SubtaskFactory<S> subtaskFactory) {
            return securityRealms(new SecurityRealmsMigration.Builder<S>().subtask(subtaskFactory).build());
        }

        public Builder<S> interfaces(final InterfacesMigration<S> resourcesMigration) {
            return subtask(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getInterfacesManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> interfaces(final InterfacesMigration.SubtaskFactory<S> subtaskFactory) {
            return interfaces(new InterfacesMigration.Builder<S>().subtask(subtaskFactory).build());
        }

        public Builder<S> managementInterfaces(final ManagementInterfacesMigration<S> resourcesMigration) {
            return subtask(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getManagementInterfacesManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> managementInterfaces(final ManagementInterfacesMigration.SubtaskFactory<S> subtaskFactory) {
            return managementInterfaces(new ManagementInterfacesMigration.Builder<S>().subtask(subtaskFactory).build());
        }

        public Builder<S> jvms(final JVMsMigration<S> resourcesMigration) {
            return subtask(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getJVMsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> jvms(final JVMsMigration.SubtaskFactory<S> subtaskFactory) {
            return jvms(new JVMsMigration.Builder<S>().subtask(subtaskFactory).build());
        }
*/
        public HostMigration<S> build() {
            return new HostMigration(this);
        }
    }
}
