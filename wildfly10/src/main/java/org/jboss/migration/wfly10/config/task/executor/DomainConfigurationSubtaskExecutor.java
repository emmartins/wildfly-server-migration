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

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ServerGroupsManagement;

/**
 * @author emmartins
 */
public class DomainConfigurationSubtaskExecutor {

    public static <S> ManageableServerConfigurationSubtaskExecutor<S, HostControllerConfiguration> allServerGroupJVMs(final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ManageableServerConfigurationSubtaskExecutor<S, HostControllerConfiguration>() {
            @Override
            public void executeSubtasks(S source, HostControllerConfiguration configuration, ServerMigrationTaskContext context) throws Exception {
                final ServerGroupsManagement resourcesManagement = configuration.getServerGroupsManagement();
                for (String resourceName : resourcesManagement.getResourceNames()) {
                    subtaskExecutor.executeSubtasks(source, resourcesManagement.getServerGroupManagement(resourceName).getJVMsManagement(), context);
                }
            }
        };
    }

    public static <S> ManageableServerConfigurationSubtaskExecutor<S, HostControllerConfiguration> serverGroupJVMs(final String serverGroup, final JVMsManagementSubtaskExecutor<S> subtaskExecutor) {
        return new ManageableServerConfigurationSubtaskExecutor<S, HostControllerConfiguration>() {
            @Override
            public void executeSubtasks(S source, HostControllerConfiguration configuration, ServerMigrationTaskContext context) throws Exception {
                subtaskExecutor.executeSubtasks(source, configuration.getServerGroupsManagement().getServerGroupManagement(serverGroup).getJVMsManagement(), context);
            }
        };
    }

    private DomainConfigurationSubtaskExecutor() {}

}
