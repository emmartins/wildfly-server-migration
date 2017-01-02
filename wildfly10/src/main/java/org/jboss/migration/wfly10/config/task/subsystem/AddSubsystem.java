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
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

/**
 * A task which creates a subsystem if its missing from the server's config.
 * @author emmartins
 */
public class AddSubsystem implements UpdateSubsystemTaskFactory.SubtaskFactory {

    public static final AddSubsystem INSTANCE = new AddSubsystem();

    protected AddSubsystem() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, final UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement) {
        return new UpdateSubsystemTaskFactory.Subtask(config, subsystem, subsystemsManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return new ServerMigrationTaskName.Builder("add-subsystem-config").addAttribute("name", subsystem.getName()).build();
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config != null) {
                    context.getLogger().infof("Skipped adding subsystem %s, already exists in config.", subsystem.getName());
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().debugf("Adding subsystem %s config...", subsystem.getName());
                addSubsystem(subsystem, subsystemsManagement, context);
                context.getLogger().infof("Subsystem %s config added.", subsystem.getName());
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    protected void addSubsystem(UpdateSubsystemTaskFactory subsystem, SubsystemsManagement subsystemsManagement, TaskContext context) throws Exception {
        final ModelNode op = Util.createAddOperation(subsystemsManagement.getResourcePathAddress(subsystem.getName()));
        subsystemsManagement.getServerConfiguration().executeManagementOperation(op);
    }
}
