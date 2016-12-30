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
import org.jboss.dmr.ModelType;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ParentServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupsManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.task.executor.SocketBindingGroupsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.factory.ManageableServerConfigurationTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Set socket binding's ports as value expressions.
 * @author emmartins
 */
public class AddSocketBindingPortExpressions<S> implements ManageableServerConfigurationTaskFactory<S, ManageableServerConfiguration> {

    public static final String[] SOCKET_BINDINGS = {
            "ajp",
            "http",
            "https"
    };

    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder("add-socket-binding-port-expressions").build();

    public static final AddSocketBindingPortExpressions INSTANCE = new AddSocketBindingPortExpressions();

    private AddSocketBindingPortExpressions() {
    }

    @Override
    public ServerMigrationTask getTask(S source, ManageableServerConfiguration configuration) throws Exception {
        return new ParentServerMigrationTask.Builder(TASK_NAME)
                .subtask(SubtaskExecutorAdapters.of(source, configuration, new SubtaskExecutor<S>()))
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Adding socket binding's port expressions...");
                    }

                    @Override
                    public void done(ServerMigrationTaskContext context) {
                        context.getLogger().infof("Socket binding's port expressions added.");
                    }
                })
                .build();
    }

    public static class SubtaskExecutor<S> implements SocketBindingGroupsManagementSubtaskExecutor<S> {
        @Override
        public void executeSubtasks(S source, SocketBindingGroupsManagement resourceManagement, ServerMigrationTaskContext context) throws Exception {
            for (String socketBindingGroupName : resourceManagement.getResourceNames()) {
                context.getLogger().debugf("Processing socket binding group %s...", socketBindingGroupName);
                final SocketBindingGroupManagement socketBindingGroupManagement = resourceManagement.getSocketBindingGroupManagement(socketBindingGroupName);
                for (String socketBindingName : SOCKET_BINDINGS) {
                    final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("add-"+socketBindingName+"-port-expression")
                            .addAttribute("group", socketBindingGroupManagement.getSocketBindingGroupName())
                            .build();
                    final String propertyName = "jboss."+socketBindingName+".port";
                    final AddSocketBindingPortExpression subtask = new AddSocketBindingPortExpression(socketBindingName, subtaskName, propertyName , socketBindingGroupManagement.getSocketBindingsManagement());
                    context.execute(new SkippableByEnvServerMigrationTask(subtask, TASK_NAME + "." + subtaskName.getName() + ".skip"));
                }
            }
        }
    }

    static class AddSocketBindingPortExpression implements ServerMigrationTask {

        private final String resourceName;
        private final String propertyName;
        private final ServerMigrationTaskName taskName;
        private final SocketBindingsManagement resourceManagement;

        AddSocketBindingPortExpression(String resourceName, ServerMigrationTaskName taskName, String propertyName, SocketBindingsManagement resourceManagement) {
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
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            // retrieve resource config
            final ModelNode resource = resourceManagement.getResource(resourceName);
            if (resource == null) {
                context.getLogger().debugf("Socket binding %s does not exists, task to add port property skipped.", resourceName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            // check if attribute is defined
            if (!resource.hasDefined(PORT)) {
                context.getLogger().debugf("Socket binding %s has no port defined, task to add port property skipped.", resourceName);
                return ServerMigrationTaskResult.SKIPPED;
            }
            // check current attribute value
            final ModelNode resourceAttr = resource.get(PORT);
            if (resourceAttr.getType() == ModelType.EXPRESSION) {
                context.getLogger().debugf("Socket binding %s unexpected port value %s, task to add port property skipped.", resourceName, resourceAttr.asExpression().getExpressionString());
                return ServerMigrationTaskResult.SKIPPED;
            }
            // update attribute value
            final ValueExpression valueExpression = new ValueExpression("${"+propertyName+":"+resourceAttr.asString()+"}");
            final PathAddress pathAddress = resourceManagement.getResourcePathAddress(resourceName);
            final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
            writeAttrOp.get(NAME).set(PORT);
            writeAttrOp.get(VALUE).set(valueExpression);
            resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
            context.getLogger().infof("Socket binding %s port value expression set as %s.", pathAddress.toCLIStyleString(), valueExpression.getExpressionString());
            return ServerMigrationTaskResult.SUCCESS;
        }
    }
}