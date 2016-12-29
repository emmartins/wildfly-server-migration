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
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.management.DeploymentsManagement;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfilesManagement;
import org.jboss.migration.wfly10.config.management.SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.factory.ParentManageableServerConfigurationTaskFactory;

/**
 * @author emmartins
 */
public class SubtaskExecutors {

    private SubtaskExecutors() {
    }

    public static <S> ServerGroupsManagementSubtaskExecutor<S> allServerGroups(final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ServerGroupsManagementSubtaskExecutor<S>() {
            @Override
            public void executeSubtasks(S source, ServerGroupsManagement resourceManagement, ServerMigrationTaskContext context) throws Exception {
                for (String resourceName : resourceManagement.getResourceNames()) {
                    subtaskExecutor.executeSubtasks(source, resourceManagement.getServerGroupManagement(resourceName).getJVMsManagement(), context);
                }
            }
        };
    }

    public static <S> ServerGroupsManagementSubtaskExecutor<S> serverGroup(final String resourceName, final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ServerGroupsManagementSubtaskExecutor<S>() {
            @Override
            public void executeSubtasks(S source, ServerGroupsManagement resourceManagement, ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourceManagement.getServerGroupManagement(resourceName).getJVMsManagement(), context);
            }
        };
    }

    public static class JVMs {

        public static <S> ServerGroupsManagementSubtaskExecutor<S> execute(final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
            return new ServerGroupsManagementSubtaskExecutor<S>() {
                @Override
                public void executeSubtasks(S source, ServerGroupsManagement resourceManagement, ServerMigrationTaskContext context) throws Exception {
                    for
                }
            }
        }

        public static <S> ServerGroupsManagementSubtaskExecutor<S> from(JVMsManagementSubtaskExecutor<S> subtaskExecutor) {

        }

        private ServerGroupsManagement() {
        }
    }
    ParentManageableServerConfigurationTaskFactory.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final SocketBindingGroupsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSocketBindingGroupsManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final InterfacesManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getInterfacesManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final ManageableServerConfiguration configuration, final ExtensionsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getExtensionsManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final DeploymentsManagement resourcesManagement, final DeploymentsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final SubsystemsManagement resourcesManagement, final SubsystemsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final SecurityRealmsManagement resourcesManagement, final SecurityRealmsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, resourcesManagement, context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final StandaloneServerConfiguration configuration, final SubsystemsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSubsystemsManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostConfiguration configuration, final SubsystemsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getSubsystemsManagement(), context);
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostControllerConfiguration configuration, final SubsystemsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                final ProfilesManagement profilesManagement = configuration.getProfilesManagement();
                for(String profileName : profilesManagement.getResourceNames()) {
                    context.getLogger().debugf("Processing profile %s...", profileName);
                    subtaskExecutor.executeSubtasks(source, profilesManagement.getProfileManagement(profileName).getSubsystemsManagement(), context);
                }
            }
        };
    }

    public static <S> ParentServerMigrationTask.SubtaskExecutor of(final S source, final HostConfiguration configuration, final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getJVMsManagement(), context);
            }
        };
    }
}
