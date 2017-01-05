/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemConfigSubtask;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemConfigSubtask2;

/**
 * @author emmartins
 */
public class SubsystemManagementTaskBuilders<S> {

    private SubsystemManagementTaskBuilders() {
    }

    public static <S> SubsystemManagementParentTask.Builder<S> addSubsystem(String extension, String subsystem) {
        return addSubsystem(extension, subsystem, new AddSubsystemConfigSubtask2());

    }

    public static <S> SubsystemManagementParentTask.Builder<S> addSubsystem(final String extension, final String subsystem, final AddSubsystemConfigSubtask2<S> subsystemConfigSubtask) {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder("add-subsystem").addAttribute("name", subsystem).build();
        final AbstractServerMigrationTask.Listener listener = new AbstractServerMigrationTask.Listener() {
            @Override
            public void started(TaskContext context) {
                context.getLogger().infof("Adding subsystem %s...", subsystem);
            }
            @Override
            public void done(TaskContext context) {
                if (context.hasSucessfulSubtasks()) {
                    context.getLogger().infof("Subsystem %s added.", subsystem);
                } else {
                    context.getLogger().infof("Subsystem %s not added.", subsystem);
                }
            }
        };
        return new SubsystemManagementParentTask.Builder<S>(extension, subsystem, taskName).listener(listener).subtask(subsystemConfigSubtask)
    }

}
