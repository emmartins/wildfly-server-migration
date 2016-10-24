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

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostsManagement;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostConfiguration;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class HostMigration<S> implements HostsMigration.SubtaskFactory<S> {

    public static final String HOST = "host";

    public static final String MIGRATION_REPORT_TASK_ATTR_NAME = "name";

    protected final HostConfigurationProvider hostConfigurationProvider;
    protected final List<SubtaskFactory<S>> subtaskFactories;

    protected HostMigration(Builder builder) {
        this.hostConfigurationProvider = builder.hostConfigurationProvider;
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    @Override
    public void addSubtasks(S source, HostsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceManagement.getResourceNames()) {
            addResourceSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addResourceSubtask(final String resourceName, final S source, final HostsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder(HOST).addAttribute(MIGRATION_REPORT_TASK_ATTR_NAME, resourceName).build();
        subtasks.add(new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return subtaskName;
            }

            @Override
            public ServerMigrationTaskResult run(final ServerMigrationTaskContext context) throws Exception {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                context.getLogger().infof("Migrating host %s in host configuration %s ...", resourceName, source);
                final HostConfiguration hostConfiguration = hostConfigurationProvider.getHostConfiguration(resourceName, resourceManagement.getHostControllerConfiguration());
                hostConfiguration.start();
                try {
                    final ServerMigrationTasks serverMigrationTasks = new ServerMigrationTasks() {
                        @Override
                        public void add(ServerMigrationTask task) {
                            context.execute(task);
                        }

                        @Override
                        public void addAll(Collection<ServerMigrationTask> tasks) {
                            for (ServerMigrationTask task : tasks) {
                                add(task);
                            }
                        }
                    };
                    for (SubtaskFactory<S> subtaskFactory : subtaskFactories) {
                        subtaskFactory.addSubtasks(source, hostConfiguration, serverMigrationTasks);
                    }
                } finally {
                    hostConfiguration.stop();
                    context.getLogger().infof("Migration of host %s in host configuration %s done.", resourceName, source);
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        });
    }

    /**
     * Subtasks factory.
     * @param <S> the source for the configuration
     */
    public interface SubtaskFactory<S> {
        void addSubtasks(S source, HostConfiguration hostConfiguration, ServerMigrationTasks subtasks) throws Exception;
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
        private final List<SubtaskFactory<S>> subtaskFactories;

        public Builder() {
            this(new EmbeddedHostConfiguration.HostConfigFileMigrationFactory());
        }

        public Builder(HostConfigurationProvider hostConfigurationProvider) {
            this.hostConfigurationProvider = hostConfigurationProvider;
            subtaskFactories = new ArrayList<>();
        }

        public Builder<S> addSubtaskFactory(SubtaskFactory<S> subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public Builder<S> addSubsystemsMigration(final SubsystemsMigration<S> resourcesMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getSubsystemsManagementTask(source, configuration.getSubsystemsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> addSecurityRealmsMigration(final SecurityRealmsMigration<S> resourcesMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getSecurityRealmsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> addInterfacesMigration(final InterfacesMigration<S> resourcesMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getInterfacesManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> addManagementInterfacesMigration(final ManagementInterfacesMigration<S> resourcesMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getManagementInterfacesManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public Builder<S> addJVMsMigration(final JVMsMigration<S> resourcesMigration) {
            return addSubtaskFactory(new SubtaskFactory<S>() {
                @Override
                public void addSubtasks(S source, HostConfiguration configuration, ServerMigrationTasks subtasks) throws Exception {
                    final ServerMigrationTask subtask = resourcesMigration.getTask(source, configuration.getJVMsManagement());
                    if (subtask != null) {
                        subtasks.add(subtask);
                    }
                }
            });
        }

        public HostMigration<S> build() {
            return new HostMigration(this);
        }
    }
}
