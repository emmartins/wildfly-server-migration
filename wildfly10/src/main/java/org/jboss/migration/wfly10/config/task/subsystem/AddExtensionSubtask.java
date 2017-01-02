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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.TaskContextImpl;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.task.executor.ExtensionsManagementSubtaskExecutor;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;

/**
 * A task which creates an extension if its missing from the server's config.
 * @author emmartins
 */
public class AddExtensionSubtask<S> implements ExtensionsManagementSubtaskExecutor<S> {

    private final String extensionModule;

    public AddExtensionSubtask(String extensionModule) {
        this.extensionModule = extensionModule;
    }

    @Override
    public void executeSubtasks(S source, final ExtensionsManagement extensionsManagement, TaskContext context) throws Exception {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("add-extension").addAttribute("name", extensionModule).build();
        final ServerMigrationTask task = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }
            @Override
            public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                if (!extensionsManagement.getResourceNames().contains(extensionModule)) {
                    context.getLogger().debugf("Adding Extension %s...", extensionModule);
                    final ModelNode op = Util.createAddOperation(extensionsManagement.getResourcePathAddress(extensionModule));
                    op.get(MODULE).set(extensionModule);
                    extensionsManagement.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().infof("Extension %s added.",extensionModule);
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    context.getLogger().infof("Skipped adding extension %s, already exists in config.", extensionModule);
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
        context.execute(task);
    }
}
