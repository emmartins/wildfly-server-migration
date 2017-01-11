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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.factory.AbstractSubsystemConfigurationTask;

import java.io.IOException;

/**
 * @author emmartins
 */
public abstract class AbstractSubsystemTask<S> extends AbstractServerMigrationTask {

    private final S source;
    private final String extension;
    private final String subsystem;
    private final SubsystemsManagement subsystemsManagement;
    private final TaskEnvironment taskEnvironment;

    protected AbstractSubsystemTask(ServerMigrationTaskName taskName, AbstractSubsystemConfigurationTask.Context<S> parentContext) {
        this(taskName, parentContext.getSource(), parentContext.getExtension(), parentContext.getSubsystem(), parentContext.getResourcesManagement(), new TaskEnvironment(parentContext.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(parentContext.getSubsystem(), taskName.getName())));
    }

    protected AbstractSubsystemTask(ServerMigrationTaskName taskName, S source, String extension, String subsystem, SubsystemsManagement subsystemsManagement, final TaskEnvironment taskEnvironment) {
        super(new AbstractServerMigrationTask.Builder(taskName).skipper(new Skipper() {
            @Override
            public boolean isSkipped(TaskContext context) {
                return taskEnvironment.isSkippedByEnvironment();
            }
        }));
        this.source = source;
        this.extension = extension;
        this.subsystem = subsystem;
        this.subsystemsManagement = subsystemsManagement;
        this.taskEnvironment = taskEnvironment;
    }

    protected ModelNode getConfig() throws IOException {
        return getSubsystemsManagement().getResource(getSubsystem());
    }

    protected String getConfigName() {
        return getPathAddress().toCLIStyleString();
    }

    protected String getExtension() {
        return extension;
    }

    protected PathAddress getPathAddress() {
        return getSubsystemsManagement().getResourcePathAddress(getSubsystem());
    }

    protected String getSubsystem() {
        return subsystem;
    }

    protected SubsystemsManagement getSubsystemsManagement() {
        return subsystemsManagement;
    }
}
