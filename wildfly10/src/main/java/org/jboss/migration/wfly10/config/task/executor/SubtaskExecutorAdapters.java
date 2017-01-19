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

package org.jboss.migration.wfly10.config.task.executor;

import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextImpl;
import org.jboss.migration.wfly10.config.management.DeploymentResources;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileResources;
import org.jboss.migration.wfly10.config.management.SecurityRealmResources;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.management.subsystem.SubsystemsConfigurationSubtasks;

/**
 * @author emmartins
 */
public class SubtaskExecutorAdapters {

    public static final SubtaskExecutorAdapters INSTANCE = new SubtaskExecutorAdapters();

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final SocketBindingGroupsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSocketBindingGroupResources(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final InterfacesManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getInterfacesManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final ExtensionsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getExtensionResources(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final DeploymentResources resourcesManagement, final DeploymentsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContextImpl context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final SubsystemResources resourcesManagement, final SubsystemsConfigurationSubtasks<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final SecurityRealmResources resourcesManagement, final SecurityRealmsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final StandaloneServerConfiguration configuration, final SubsystemsConfigurationSubtasks<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSubsystemResources(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostConfiguration configuration, final SubsystemsConfigurationSubtasks<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSubsystemResources(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostControllerConfiguration configuration, final SubsystemsConfigurationSubtasks<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                final ProfileResources profileResources = configuration.getProfileResources();
                for(String profileName : profileResources.getResourceNames()) {
                    context.getLogger().debugf("Processing profile %s...", profileName);
                    subtaskExecutor.executeSubtasks(source, profileResources.getResource(profileName).getSubsystemsManagement(), context);
                }
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostConfiguration configuration, final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(TaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getJvmResources(), context);
            }
        };
    }
}
