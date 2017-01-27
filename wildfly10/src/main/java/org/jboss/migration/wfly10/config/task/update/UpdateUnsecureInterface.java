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
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ParentServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.InterfaceResources;
import org.jboss.migration.wfly10.config.task.executor.InterfacesManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Setup EAP 7 http upgrade management.
 * @author emmartins
 */
public class UpdateUnsecureInterface<S> implements DomainConfigurationTaskFactory<S>, HostConfigurationTaskFactory<S> {

    public static final UpdateUnsecureInterface INSTANCE = new UpdateUnsecureInterface();

    private static final String TASK_NAME_NAME = "update-unsecure-interface";
    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();

    private UpdateUnsecureInterface() {
    }

    @Override
    public ServerMigrationTask getTask(final S source, final HostControllerConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.SubtaskExecutor subtaskExecutor = new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final TaskContext context) throws Exception {
                new SetUnsecureInterfaceInetAddress().executeSubtasks(source, configuration.getInterfacesManagement(), context);
            }
        };
        return getTask(subtaskExecutor);
    }


    @Override
    public ServerMigrationTask getTask(final S source, final HostConfiguration configuration) throws Exception {
        final ParentServerMigrationTask.SubtaskExecutor subtaskExecutor = new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final TaskContext context) throws Exception {
                new RemoveUnsecureInterface().executeSubtasks(source, configuration.getInterfacesManagement(), context);
            }
        };
        return getTask(subtaskExecutor);
    }

    protected ServerMigrationTask getTask(ParentServerMigrationTask.SubtaskExecutor subtaskExecutor) throws Exception {
        return new ParentServerMigrationTask.Builder(TASK_NAME)
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {
                        context.getLogger().debugf("Updating unsecure interface configuration...");
                    }
                    @Override
                    public void done(TaskContext context) {
                        context.getLogger().debugf("Unsecure interface configuration updated.");
                    }
                })
                .subtask(subtaskExecutor)
                .build();
    }

    static class SetUnsecureInterfaceInetAddress<S> implements InterfacesManagementSubtaskExecutor<S> {

        public static final String SERVER_MIGRATION_TASK_NAME_NAME = "set-unsecure-interface-inet-address";
        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
        private static final String INTERFACE_NAME = "unsecure";

        @Override
        public void executeSubtasks(S source, final InterfaceResources resourceManagement, TaskContext context) throws Exception {
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SERVER_MIGRATION_TASK_NAME;
                }
                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    // retrieve resource config
                    final ModelNode resource = resourceManagement.getResourceConfiguration(INTERFACE_NAME);
                    if (resource == null) {
                        context.getLogger().debugf("Interface %s does not exists.", INTERFACE_NAME);
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    // check if attribute is defined
                    if (resource.hasDefined(INET_ADDRESS)) {
                        context.getLogger().debugf("Interface %s inet address already defined.", INTERFACE_NAME);
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    // set attribute value
                    final ValueExpression valueExpression = new ValueExpression("${jboss.bind.address.unsecure:127.0.0.1}");
                    final PathAddress pathAddress = resourceManagement.getResourcePathAddress(INTERFACE_NAME);
                    final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    writeAttrOp.get(NAME).set(INET_ADDRESS);
                    writeAttrOp.get(VALUE).set(valueExpression);
                    resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                    context.getLogger().infof("Interface %s inet address value set as %s.", INTERFACE_NAME, valueExpression.getExpressionString());
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            context.execute(new SkippableByEnvServerMigrationTask(subtask, UpdateUnsecureInterface.TASK_NAME_NAME+"."+SERVER_MIGRATION_TASK_NAME_NAME+".skip"));
        }
    }

    static class RemoveUnsecureInterface<S> implements InterfacesManagementSubtaskExecutor<S> {

        public static final String SERVER_MIGRATION_TASK_NAME_NAME = "remove-unsecure-interface";
        public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).build();
        private static final String INTERFACE_NAME = "unsecure";

        @Override
        public void executeSubtasks(S source, final InterfaceResources resourceManagement, TaskContext context) throws Exception {
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SERVER_MIGRATION_TASK_NAME;
                }
                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    // retrieve resource config
                    if (!resourceManagement.getResourceNames().contains(INTERFACE_NAME)) {
                        context.getLogger().debugf("Interface %s does not exists.", INTERFACE_NAME);
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    final PathAddress pathAddress = resourceManagement.getResourcePathAddress(INTERFACE_NAME);
                    final ModelNode removeOp = Util.createRemoveOperation(pathAddress);
                    resourceManagement.getServerConfiguration().executeManagementOperation(removeOp);
                    context.getLogger().infof("Interface %s removed.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            context.execute(new SkippableByEnvServerMigrationTask(subtask, UpdateUnsecureInterface.TASK_NAME_NAME+"."+SERVER_MIGRATION_TASK_NAME_NAME+".skip"));
        }
    }
}