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
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * Updates unsecure interface.
 * @author emmartins
 */
public class UpdateUnsecureInterface<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    private static final String INTERFACE_NAME = "unsecure";
    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder("interface."+INTERFACE_NAME+".update").build();

    public UpdateUnsecureInterface() {
        name(TASK_NAME);
        skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().infof("Unsecure interface update task starting..."));
        subtasks(InterfaceResource.class, INTERFACE_NAME, ManageableResourceCompositeSubtasks.of(new SetUnsecureInterfaceInetAddress<>()));
        afterRun(context -> context.getLogger().debugf("Unsecure interface update task done."));
    }


    public static class SetUnsecureInterfaceInetAddress<S> extends ManageableResourceLeafTask.Builder<S, InterfaceResource> {
        private static final ServerMigrationTaskName SUBTASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME.getName()+".set-inet-address").build();
        protected SetUnsecureInterfaceInetAddress() {
            name(SUBTASK_NAME);
            skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
            final ManageableResourceTaskRunnableBuilder<S, InterfaceResource> runnableBuilder = params -> context -> {
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
            runBuilder(runnableBuilder);
        }
    }
}