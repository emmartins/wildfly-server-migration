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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerMigrationTasks;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.config.management.SocketBindingsManagement;
import org.jboss.migration.wfly10.config.task.SocketBindingsMigration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Set socket binding's ports as value expressions.
 * @author emmartins
 */
public class SetSocketBindingPortExpressions implements SocketBindingsMigration.SubtaskFactory<ServerPath<EAP6Server>> {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "set-port-expression";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTR_SOCKET_BINDING = "socket-binding";
    private final String[] resourceNames;

    public SetSocketBindingPortExpressions(String ...resourceNames) {
        this.resourceNames = resourceNames.clone();
    }

    @Override
    public void addSubtasks(ServerPath<EAP6Server> source, SocketBindingsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        for (String resourceName : resourceNames) {
            addSubtask(resourceName, source, resourceManagement, subtasks);
        }
    }

    protected void addSubtask(String resourceName, ServerPath<EAP6Server> source, final SocketBindingsManagement resourceManagement, ServerMigrationTasks subtasks) throws Exception {
        final String propertyName = "jboss."+resourceName+".port";
        final Task task = new Task(resourceName, propertyName , resourceManagement);
        final String envSkipProperty = SocketBindingsMigration.SOCKET_BINDING + "." + resourceName + "." + SERVER_MIGRATION_TASK_NAME_NAME + ".skip";
        subtasks.add(new SkippableByEnvServerMigrationTask(task, envSkipProperty));
    }

    static class Task implements ServerMigrationTask {

        private static final String PORT = "port";

        private final String resourceName;
        private final String propertyName;
        private final ServerMigrationTaskName taskName;
        private final SocketBindingsManagement resourceManagement;

        Task(String resourceName, String propertyName, SocketBindingsManagement resourceManagement) {
            this.resourceName = resourceName;
            this.propertyName = propertyName;
            this.resourceManagement = resourceManagement;
            this.taskName = new ServerMigrationTaskName.Builder(SERVER_MIGRATION_TASK_NAME_NAME).addAttribute(SERVER_MIGRATION_TASK_NAME_ATTR_SOCKET_BINDING, resourceName).build();
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
            final ModelNode socketBindingPort = resource.get(PORT);
            if (socketBindingPort.getType() == ModelType.EXPRESSION) {
                context.getLogger().debugf("Socket binding %s unexpected port value %s, task to add port property skipped.", resourceName, socketBindingPort.asExpression());
                return ServerMigrationTaskResult.SKIPPED;
            }
            // update attribute value
            final ValueExpression valueExpression = new ValueExpression("${"+propertyName+":"+socketBindingPort.asString()+"}");
            final PathAddress pathAddress = resourceManagement.getResourcePathAddress(resourceName);
            final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
            writeAttrOp.get(NAME).set(PORT);
            writeAttrOp.get(VALUE).set(valueExpression);
            resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
            context.getLogger().infof("Socket binding %s port value expression set as %s.", resourceName, valueExpression);
            return ServerMigrationTaskResult.SUCCESS;
        }
    }
}