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
import org.jboss.dmr.ModelType;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ParentServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResources;
import org.jboss.migration.wfly10.config.management.SocketBindingResources;
import org.jboss.migration.wfly10.config.task.executor.SocketBindingGroupsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Set socket binding's multicast address as value expressions.
 * @author emmartins
 */
public class AddSocketBindingMulticastAddressExpressions<S> extends ServerConfigurationCompositeTask.Builder<S> {

    public static final String[] SOCKET_BINDINGS = {
            "modcluster"
    };
    
    public AddSocketBindingMulticastAddressExpressions() {
        name("add-socket-binding-multicast-address-expressions");
        beforeRun(context -> context.getLogger().infof("Adding socket binding's multicast address expressions..."));
        afterRun( context -> context.getLogger().infof("Socket binding's multicast address expressions added."));
    }

    @Override
    public ServerMigrationTask getTask(S source, ManageableServerConfiguration configuration) throws Exception {
        return new ParentServerMigrationTask.Builder(TASK_NAME)
                .subtask(SubtaskExecutorAdapters.of(source, configuration, new SubtaskExecutor<S>()))
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {

                    }

                    @Override
                    public void done(TaskContext context) {
                        ;
                    }
                })
                .build();
    }

    public static class SubtaskExecutor<S> implements SocketBindingGroupsManagementSubtaskExecutor<S> {
        @Override
        public void executeSubtasks(S source, SocketBindingGroupResources resourceManagement, TaskContext context) throws Exception {
            for (String socketBindingGroupName : resourceManagement.getResourceNames()) {
                context.getLogger().debugf("Processing socket binding group %s...", socketBindingGroupName);
                final SocketBindingGroupManagement socketBindingGroupManagement = resourceManagement.getSocketBindingGroupManagement(socketBindingGroupName);
                for (String socketBindingName : SOCKET_BINDINGS) {
                    final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("add-"+socketBindingName+"-multicast-address-expression")
                            .addAttribute("group", socketBindingGroupManagement.getSocketBindingGroupName())
                            .build();
                    final String propertyName = "jboss."+socketBindingName+".multicast.adress";
                    final AddSocketBindingMulticastAddressExpression subtask = new AddSocketBindingMulticastAddressExpression(socketBindingName, subtaskName, propertyName , socketBindingGroupManagement.getSocketBindingsManagement());
                    context.execute(new SkippableByEnvServerMigrationTask(subtask, TASK_NAME + "." + subtaskName.getName() + ".skip"));
                }
            }
        }
    }

    static class AddSocketBindingMulticastAddressExpression implements ServerMigrationTask {

        private final String resourceName;
        private final String propertyName;
        private final ServerMigrationTaskName taskName;
        private final SocketBindingResources resourceManagement;

        AddSocketBindingMulticastAddressExpression(String resourceName, ServerMigrationTaskName taskName, String propertyName, SocketBindingResources resourceManagement) {
            this.resourceName = resourceName;
            this.propertyName = propertyName;
            this.resourceManagement = resourceManagement;
            this.taskName = taskName;
        }

        @Override
        public ServerMigrationTaskName getName() {
            return taskName;
        }

        @Override
        public ServerMigrationTaskResult run(TaskContext context) throws Exception {
            // retrieve resource config
            final ModelNode resource = resourceManagement.getResourceConfiguration(resourceName);
            if (resource == null) {
                context.getLogger().debugf("Socket binding %s does not exists, task to add multicast address property skipped.", resourceName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            // check if attribute is defined
            if (!resource.hasDefined(MULTICAST_ADDRESS)) {
                context.getLogger().debugf("Socket binding %s has no multicast address defined, task to add multicast address property skipped.", resourceName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            // check current attribute value
            final ModelNode resourceAttr = resource.get(MULTICAST_ADDRESS);
            if (resourceAttr.getType() == ModelType.EXPRESSION) {
                context.getLogger().debugf("Socket binding %s unexpected multicast address value %s, task to add multicast address property skipped.", resourceName, resourceAttr.asExpression().getExpressionString());
                return ServerMigrationTaskResult.SKIPPED;
            }
            // update attribute value
            final ValueExpression valueExpression = new ValueExpression("${"+propertyName+":"+resourceAttr.asString()+"}");
            final PathAddress pathAddress = resourceManagement.getResourcePathAddress(resourceName);
            final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
            writeAttrOp.get(NAME).set(MULTICAST_ADDRESS);
            writeAttrOp.get(VALUE).set(valueExpression);
            resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
            context.getLogger().infof("Socket binding %s multicast address value expression set as %s.", pathAddress.toCLIStyleString(), valueExpression.getExpressionString());
            return ServerMigrationTaskResult.SUCCESS;
        }
    }
}