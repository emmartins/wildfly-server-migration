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
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Setup EAP 7 http upgrade management.
 * @author emmartins
 */
public class UpdateUnsecureInterface<S> extends ServerConfigurationCompositeTask.Builder<S> {

    private static final String INTERFACE_NAME = "unsecure";

    public UpdateUnsecureInterface() {
        name("update-unsecure-interface");
        beforeRun(context -> context.getLogger().debugf("Updating unsecure interface configuration..."));
        subtasks(InterfaceResource.class, INTERFACE_NAME, ResourceCompositeSubtasks.of(new SetUnsecureInterfaceInetAddress<>()));
        afterRun(context -> context.getLogger().debugf("Unsecure interface configuration updated."));
    }

    public static class SetUnsecureInterfaceInetAddress<S> extends ResourceLeafTask.Builder<S, InterfaceResource> {
        protected SetUnsecureInterfaceInetAddress() {
            name("set-unsecure-interface-inet-address");
            final ResourceTaskRunnableBuilder<S, InterfaceResource> runnableBuilder = (params, taskName) -> context -> {
                final InterfaceResource resource = params.getResource();
                final ModelNode resourceConfig = params.getResource().getResourceConfiguration();
                if (resourceConfig == null) {
                    context.getLogger().debugf("Interface %s does not exists.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // check if attribute is defined
                if (resourceConfig.hasDefined(INET_ADDRESS)) {
                    context.getLogger().debugf("Interface %s inet address already defined.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // set attribute value
                final ValueExpression valueExpression = new ValueExpression("${jboss.bind.address.unsecure:127.0.0.1}");
                final PathAddress pathAddress = resource.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(INET_ADDRESS);
                writeAttrOp.get(VALUE).set(valueExpression);
                resource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().infof("Interface %s inet address value set as %s.", INTERFACE_NAME, valueExpression.getExpressionString());
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(runnableBuilder);
        }
    }
}