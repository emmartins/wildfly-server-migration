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

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.HostResource;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostConfiguration;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostsManagementTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParametersImpl;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceComponentTaskBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesComponentTaskBuilder;

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

    protected HostMigration(Builder<S> builder) {
        this.hostConfigurationProvider = builder.hostConfigurationProvider;
        this.subtaskFactories = Collections.unmodifiableList(builder.subtaskFactories);
    }

    @Override
    public ServerMigrationTask getTask(final S source, final HostResource.Parent hostsManagement) {
        return new ManageableResourceCompositeTask.Builder<S, HostResource.Parent>()
                .name(HOSTS)
                .beforeRun(context -> context.getLogger().debugf("Hosts migration starting..."))
                .subtasks(new ManageableResourceCompositeSubtasks.Builder<S, HostResource.Parent>().subtask(HostResource.class, getSubtask()))
                .afterRun(context -> context.getLogger().debugf("Hosts migration done."))
                .build(new ManageableResourceBuildParametersImpl<>(source, hostsManagement));
    }

    protected ManageableResourceComponentTaskBuilder<S, HostResource, ?> getSubtask() {
        return new ManageableResourceLeafTask.Builder<S, HostResource>()
                .nameBuilder(parameters -> new ServerMigrationTaskName.Builder(HOST).addAttribute(MIGRATION_REPORT_TASK_ATTR_NAME, parameters.getResource().getResourceName()).build())
                .beforeRunBuilder(parameters -> context -> {
                    context.getLogger().infof("Migrating host %s...", parameters.getResource().getResourceName());
                })
                .afterRunBuilder(parameters -> context -> context.getLogger().debugf("Migration of host %s in host configuration %s done.", parameters.getResource().getResourceName(), parameters.getSource()))
                .runBuilder(params -> context -> {
                    final HostConfiguration hostConfiguration = hostConfigurationProvider.getHostConfiguration(params.getResource().getResourceName(), (HostControllerConfiguration) params.getResource().getServerConfiguration());
                    hostConfiguration.start();
                    try {
                        for (HostConfigurationTaskFactory<S> subtaskFactory : subtaskFactories) {
                            final ServerMigrationTask subtask = subtaskFactory.getTask(params.getSource(), hostConfiguration);
                            if (subtask != null) {
                                context.execute(subtask);
                            }
                        }
                    } finally {
                        hostConfiguration.stop();
                    }
                    return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
                });
    }

    /**
     * Provider for the host manageable configuration
     */
    public interface HostConfigurationProvider {
        HostConfiguration getHostConfiguration(String host, HostControllerConfiguration hostController);
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

        public Builder<S> subtask(final ManageableServerConfigurationComponentTaskBuilder<S, ?> subtaskBuilder) {
            return subtask(ManageableServerConfigurationTaskFactory.of(subtaskBuilder));
        }

        public Builder<S> subtask(final ManageableResourceComponentTaskBuilder<S, ManageableResource, ?> subtaskBuilder) {
            return subtask(ManageableServerConfigurationTaskFactory.of(subtaskBuilder));
        }

        public Builder<S> subtask(final ManageableResourcesComponentTaskBuilder<S, ManageableResource, ?> subtaskBuilder) {
            return subtask(ManageableServerConfigurationTaskFactory.of(subtaskBuilder));
        }

        public Builder<S> subtask(final ManageableServerConfigurationTaskFactory<S, HostConfiguration> subtaskFactory) {
            return subtask(new HostConfigurationTaskFactory<S>() {
                @Override
                public ServerMigrationTask getTask(S source, HostConfiguration configuration) {
                    return subtaskFactory.getTask(source, configuration);
                }
            });
        }

        public HostMigration<S> build() {
            return new HostMigration<>(this);
        }
    }
}
