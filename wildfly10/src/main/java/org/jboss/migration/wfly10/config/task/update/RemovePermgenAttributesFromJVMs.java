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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.JVMsManagement;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.DomainConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.JVMsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ManageableServerConfigurationSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.ParentManageableServerConfigurationTaskFactory;

/**
 * Removes permgen from JVM Configs.
 * @author emmartins
 */
public class RemovePermgenAttributesFromJVMs<S> implements DomainConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S> {

    public static <S> ManageableServerConfigurationTaskFactory<S, HostConfiguration> getHostConfigurationTaskFactory() {

    }

    public static <S> ManageableServerConfigurationTaskFactory<S, HostControllerConfiguration> getHostControllerConfigurationTaskFactory() {
        return getTaskFactory(DomainConfigurationSubtaskExecutor.allServerGroupJVMs(new JVMsSubtaskExecutor<S>()));
    }

    private static <S, T extends ManageableServerConfiguration> ManageableServerConfigurationTaskFactory<S, T> getTaskFactory(ManageableServerConfigurationSubtaskExecutor<S, T> subtaskExecutor) {
        return new ParentManageableServerConfigurationTaskFactory.Builder<S, T>(new ServerMigrationTaskName.Builder("remove-permgen-attributes-from-jvms").build())
                .subtask(subtaskExecutor)
                .eventListener(new ParentServerMigrationTask.EventListener() {
                    @Override
                    public void started(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Removal of permgen attributes from JVM configs starting...");
                    }
                    @Override
                    public void done(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Removal of permgen attributes from JVM configs done.");
                    }
                })
                .build();
    }

    @Override
    public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
        return getTaskFactory(new ParentManageableServerConfigurationTaskFactory.SubtaskExecutor<S, HostConfiguration>() {
            @Override
            public void executeSubtasks(S source, HostConfiguration configuration, ServerMigrationTaskContext context) throws Exception {
                JVMsSubtaskExecutor.INSTANCE.executeSubtasks(source, configuration.getJVMsManagement(), context);
            }
        });
    }

    @Override
    public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
        return null;
    }

    static class JVMsSubtaskExecutor<S> implements JVMsManagementSubtaskExecutor<S> {
        @Override
        public void executeSubtasks(S source, final JVMsManagement resourceManagement, ServerMigrationTaskContext context) throws Exception {
            for (final String resourceName : resourceManagement.getResourceNames()) {
                final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("remove-permgen-attributes-from-jvm")
                        .addAttribute("resource", resourceManagement.getResourcePathAddress(resourceName).toCLIStyleString())
                        .build();
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
                        context.getLogger().infof("Permgen removed from JVM %s", pathAddress.toCLIStyleString());
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }
}