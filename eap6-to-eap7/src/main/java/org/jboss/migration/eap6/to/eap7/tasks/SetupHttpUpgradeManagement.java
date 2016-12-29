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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ManagementInterfacesManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SocketBindingGroupsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.StandaloneServerConfigurationTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Setup EAP 7 http upgrade management.
 * @author emmartins
 */
public class SetupHttpUpgradeManagement<S> implements StandaloneServerConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S> {

    public static final SetupHttpUpgradeManagement INSTANCE = new SetupHttpUpgradeManagement();

    private static final String TASK_NAME_NAME = "setup-http-upgrade-management";
    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();

    private SetupHttpUpgradeManagement() {
    }

    @Override
    public ServerMigrationTask getTask(final S source, final StandaloneServerConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.SubtaskExecutor subtaskExecutor = new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final ServerMigrationTaskContext context) throws Exception {
                new SetManagementInterfacesHttpUpgradeEnabled().executeSubtasks(source, configuration.getManagementInterfacesManagement(), context);
                new UpdateManagementHttpsSocketBindingPort().executeSubtasks(source, configuration.getSocketBindingGroupsManagement(), context);
            }
        };
        return getTask(subtaskExecutor);
    }


    @Override
    public ServerMigrationTask getTask(final S source, final HostConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.SubtaskExecutor subtaskExecutor = new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final ServerMigrationTaskContext context) throws Exception {
                new SetManagementInterfacesHttpUpgradeEnabled().executeSubtasks(source, configuration.getManagementInterfacesManagement(), context);
            }
        };
        return getTask(subtaskExecutor);
    }

    protected ServerMigrationTask getTask(ParentServerMigrationTask.SubtaskExecutor subtaskExecutor) throws Exception {
        return new ParentServerMigrationTask.Builder(TASK_NAME)
                .eventListener(new ParentServerMigrationTask.EventListener() {
                    @Override
                    public void started(ServerMigrationTaskContext context) {
                        context.getLogger().infof("HTTP upgrade management setup starting...");
                    }
                    @Override
                    public void done(ServerMigrationTaskContext context) {
                        context.getLogger().infof("HTTP upgrade management setup completed.");
                    }
                })
                .subtask(subtaskExecutor)
                .build();
    }

    static class SetManagementInterfacesHttpUpgradeEnabled<S> implements ManagementInterfacesManagementSubtaskExecutor<S> {

        public static final String SERVER_MIGRATION_TASK_NAME_NAME = "set-management-interfaces-http-upgrade-enabled";
        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
        private static final String MANAGEMENT_INTERFACE_NAME = "http-interface";

        @Override
        public void executeSubtasks(final S source, final ManagementInterfacesManagement resourceManagement, final ServerMigrationTaskContext context) throws Exception {
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SERVER_MIGRATION_TASK_NAME;
                }
                @Override
                public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                    // retrieve resource config
                    final ModelNode resource = resourceManagement.getResource(MANAGEMENT_INTERFACE_NAME);
                    if (resource == null) {
                        context.getLogger().debugf("Management interface %s does not exists.", MANAGEMENT_INTERFACE_NAME);
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    // check if attribute is defined
                    if (resource.hasDefined(HTTP_UPGRADE_ENABLED) && resource.get(HTTP_UPGRADE_ENABLED).asBoolean()) {
                        context.getLogger().debugf("Management interface %s http upgrade already enabled.", MANAGEMENT_INTERFACE_NAME);
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    // set attribute value
                    final PathAddress pathAddress = resourceManagement.getResourcePathAddress(MANAGEMENT_INTERFACE_NAME);
                    final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    writeAttrOp.get(NAME).set(HTTP_UPGRADE_ENABLED);
                    writeAttrOp.get(VALUE).set(true);
                    resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                    context.getLogger().infof("Management interface '%s' http upgrade enabled.", MANAGEMENT_INTERFACE_NAME);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            context.execute(new SkippableByEnvServerMigrationTask(subtask, TASK_NAME_NAME+"."+SERVER_MIGRATION_TASK_NAME_NAME+".skip"));
        }
    }

    static class UpdateManagementHttpsSocketBindingPort<S> implements SocketBindingGroupsManagementSubtaskExecutor<S> {

        public static final String SERVER_MIGRATION_TASK_NAME_NAME = "update-management-https-socket-binding-port";
        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();

        public interface EnvironmentProperties {
            /**
             * the prefix for the name of the management-https socket binding related properties
             */
            String PROPERTIES_PREFIX = TASK_NAME_NAME + "." + SERVER_MIGRATION_TASK_NAME_NAME + ".";

            String PORT = PROPERTIES_PREFIX + "port";
        }

        public static final String DEFAULT_PORT = "${jboss.management.https.port:9993}";
        private final String SOCKET_BINDING_NAME = "management-https";
        private final String SOCKET_BINDING_PORT_ATTR = "port";

        @Override
        public void executeSubtasks(S source, SocketBindingGroupsManagement socketBindingGroupsManagement, ServerMigrationTaskContext context) throws Exception {
            for (String socketBindingGroup : socketBindingGroupsManagement.getResourceNames()) {
                final SocketBindingGroupManagement socketBindingGroupManagement = socketBindingGroupsManagement.getSocketBindingGroupManagement(socketBindingGroup);
                final SocketBindingsManagement socketBindingsManagement = socketBindingGroupManagement.getSocketBindingsManagement();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskName getName() {
                        return SERVER_MIGRATION_TASK_NAME;
                    }
                    @Override
                    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                        final MigrationEnvironment env = context.getServerMigrationContext().getMigrationEnvironment();
                        String envPropertyPort = env.getPropertyAsString(UpdateManagementHttpsSocketBindingPort.EnvironmentProperties.PORT);
                        if (envPropertyPort == null || envPropertyPort.isEmpty()) {
                            envPropertyPort = DEFAULT_PORT;
                        }
                        if (!socketBindingsManagement.getResourceNames().contains(SOCKET_BINDING_NAME)) {
                            return ServerMigrationTaskResult.SKIPPED;
                        }
                        // management-https binding found, update port
                        final PathAddress pathAddress = socketBindingsManagement.getResourcePathAddress(SOCKET_BINDING_NAME);
                        final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                        writeAttrOp.get(NAME).set(SOCKET_BINDING_PORT_ATTR);
                        writeAttrOp.get(VALUE).set(envPropertyPort);
                        socketBindingsManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                        context.getLogger().infof("Socket binding '%s' port set to "+envPropertyPort+".", SOCKET_BINDING_NAME);
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }
}