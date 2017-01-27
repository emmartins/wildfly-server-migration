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
import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.task.management.resource.composite.ManageableResourceCompositeTask;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.socketbinding.SocketBindingGroupResourceCompositeTask;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Set socket binding's ports as value expressions.
 * @author emmartins
 */
public class AddSocketBindingPortExpressions<S> extends SocketBindingGroupResourceCompositeTask.Builder<S> {

    public static final String[] SOCKET_BINDINGS = {
            "ajp",
            "http",
            "https"
    };

    public static final AddSocketBindingPortExpressions INSTANCE = new AddSocketBindingPortExpressions();

    private AddSocketBindingPortExpressions() {
        super(new ServerMigrationTaskName.Builder("add-socket-binding-port-expressions").build());
        listener(new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding socket binding's port expressions...");
            }

            @Override
            public void done(TaskContext context) {
                context.getLogger().infof("Socket binding's port expressions added.");
            }
        });
        for (String socketBindingName : SOCKET_BINDINGS) {
            subtask(ManageableResourceSelectors.toChild(SocketBindingResource.RESOURCE_TYPE, socketBindingName), new AddSocketBindingPortExpression<>());
        }
    }

    static class AddSocketBindingPortExpression<S> implements ManageableResourceCompositeTask.SubtaskFactory<S, SocketBindingResource> {
        @Override
        public ServerMigrationTask getTask(S source, SocketBindingResource resource, TaskContext context) throws Exception {
            final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("add-" + resource.getResourceName() + "-port-expression")
                    .addAttribute("group", resource.getParentResource().getResourceName())
                    .build();
            final String propertyName = "jboss." + resource.getResourceName() + ".port";
            final ManageableResourceLeafTask.Runnable<S, SocketBindingResource> runnable = (source1, resource1, context1) -> {
                // retrieve resource config
                final ModelNode config = resource1.getResourceConfiguration();
                // check if attribute is defined
                if (!config.hasDefined(PORT)) {
                    context1.getLogger().debugf("Socket binding %s has no port defined, task to add port property skipped.", resource1.getResourceAbsoluteName());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // check current attribute value
                final ModelNode resourceAttr = config.get(PORT);
                if (resourceAttr.getType() == ModelType.EXPRESSION) {
                    context1.getLogger().debugf("Socket binding %s unexpected port value %s, task to add port property skipped.", resource1.getResourceAbsoluteName(), resourceAttr.asExpression().getExpressionString());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // update attribute value
                final ValueExpression valueExpression = new ValueExpression("${" + propertyName + ":" + resourceAttr.asString() + "}");
                final PathAddress pathAddress = resource1.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(PORT);
                writeAttrOp.get(VALUE).set(valueExpression);
                resource1.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context1.getLogger().infof("Socket binding %s port value expression set as %s.", pathAddress.toCLIStyleString(), valueExpression.getExpressionString());
                return ServerMigrationTaskResult.SUCCESS;
            };
            return new ManageableResourceLeafTask.Builder<>(subtaskName, runnable).build(source, resource);
        }
    }
}