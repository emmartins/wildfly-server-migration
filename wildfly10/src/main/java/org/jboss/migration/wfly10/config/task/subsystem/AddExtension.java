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
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;

/**
 * A task which creates an extension if its missing from the server's config.
 * @author emmartins
 */
public class AddExtension implements WildFly10SubsystemMigrationTaskFactory {
    public static final AddExtension INSTANCE = new AddExtension();

    private AddExtension() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(final ModelNode config, final WildFly10Subsystem subsystem, final SubsystemsManagement subsystemsManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemsManagement) {

            private final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("add-extension").addAttribute("name", subsystem.getExtension().getName()).build();

            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }

            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, SubsystemsManagement subsystemsManagement, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                final String extensionName = subsystem.getExtension().getName();
                final ExtensionsManagement extensionsManagement = subsystemsManagement.getServerConfiguration().getExtensionsManagement();
                if (!extensionsManagement.getResourceNames().contains(extensionName)) {
                    context.getLogger().debugf("Adding Extension %s...", extensionName);
                    final ModelNode op = Util.createAddOperation(extensionsManagement.getResourcePathAddress(extensionName));
                    op.get(MODULE).set(extensionName);
                    subsystemsManagement.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().infof("Extension %s added.",extensionName);
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    context.getLogger().infof("Skipped adding extension %s, already exists in config.", extensionName);
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
