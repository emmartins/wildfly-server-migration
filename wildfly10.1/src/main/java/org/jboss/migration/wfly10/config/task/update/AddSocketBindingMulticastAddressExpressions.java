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
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * Set socket binding's multicast address as value expressions.
 * @author emmartins
 */
public class AddSocketBindingMulticastAddressExpressions<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    public static final String[] SOCKET_BINDINGS = {
            "modcluster"
    };

    public AddSocketBindingMulticastAddressExpressions() {
        name("socket-bindings.multicast-address.add-expressions");
        skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Adding socket binding's multicast address expressions..."));
        final ManageableServerConfigurationCompositeSubtasks.Builder<S> subtasks = new ManageableServerConfigurationCompositeSubtasks.Builder<>();
        for (String socketBinding : SOCKET_BINDINGS) {
            subtasks.subtask(SocketBindingResource.class, socketBinding, new AddSocketBindingMulticastAddressExpression<>(socketBinding));
        }
        subtasks(subtasks);
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Socket binding's multicast address expressions added.");
            } else {
                context.getLogger().debugf("No socket binding's multicast address expressions added.");
            }
        });
    }

    public static class AddSocketBindingMulticastAddressExpression<S> extends ManageableResourceLeafTask.Builder<S, SocketBindingResource> {

        protected AddSocketBindingMulticastAddressExpression(String resourceName) {
            this(resourceName, "jboss."+resourceName+".multicast.adress");
        }

        protected AddSocketBindingMulticastAddressExpression(String resourceName, String propertyName) {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("socket-binding."+parameters.getResource().getResourceName()+".multicast-address.add-expression").build());
            skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
            runBuilder(params -> context -> {
                // retrieve resource config
                final SocketBindingResource socketBindingResource = params.getResource();
                final String absoluteResourceName = socketBindingResource.getResourceAbsoluteName();
                final ModelNode resourceConfig = socketBindingResource.getResourceConfiguration();
                // check if attribute is defined
                if (!resourceConfig.hasDefined(MULTICAST_ADDRESS)) {
                    context.getLogger().debugf("Socket binding %s has no multicast address defined, task to add multicast address property skipped.", absoluteResourceName);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // check current attribute value
                final ModelNode resourceAttr = resourceConfig.get(MULTICAST_ADDRESS);
                if (resourceAttr.getType() == ModelType.EXPRESSION) {
                    context.getLogger().debugf("Socket binding %s unexpected multicast address value %s, task to add multicast address property skipped.", absoluteResourceName, resourceAttr.asExpression().getExpressionString());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // update attribute value
                final ValueExpression valueExpression = new ValueExpression("${"+propertyName+":"+resourceAttr.asString()+"}");
                final PathAddress pathAddress = socketBindingResource.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(MULTICAST_ADDRESS);
                writeAttrOp.get(VALUE).set(valueExpression);
                socketBindingResource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().debugf("Socket binding %s multicast address value expression set as %s.", absoluteResourceName, valueExpression.getExpressionString());
                return ServerMigrationTaskResult.SUCCESS;
            });
        }
    }
}