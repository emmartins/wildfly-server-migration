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
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

/**
 * Removes permgen from JVM Configs.
 * @author emmartins
 */
public class RemovePermgenAttributesFromJVMConfigs<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    public RemovePermgenAttributesFromJVMConfigs() {
        name("jvms.remove-permgen-attributes");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Removing JVM's permgen attributes..."));
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("JVM's permgen attributes removed.");
            }
        });
        subtasks(JvmResource.class, ManageableResourceCompositeSubtasks.of(new Subtask<>()));
    }

    public static class Subtask<S> extends ManageableResourceLeafTask.Builder<S, JvmResource> {
        protected Subtask() {
            nameBuilder(parameters -> new ServerMigrationTaskName.Builder("jvm."+parameters.getResource().getResourceName()+".remove-permgen-attributes").build());
            final ManageableResourceTaskRunnableBuilder<S, JvmResource> runnableBuilder = params-> context -> {
                final JvmResource resource = params.getResource();
                final ModelNode config = resource.getResourceConfiguration();
                final PathAddress pathAddress = resource.getResourcePathAddress();
                boolean updated = false;
                if (config.hasDefined("permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "permgen-size");
                    resource.getServerConfiguration().executeManagementOperation(op);
                    updated = true;
                }
                if (config.hasDefined("max-permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "max-permgen-size");
                    resource.getServerConfiguration().executeManagementOperation(op);
                    updated = true;
                }
                if (!updated) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().debugf("JVM %s permgen attributes removed.", resource.getResourceAbsoluteName());
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}