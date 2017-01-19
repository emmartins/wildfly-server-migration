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

package org.jboss.migration.wfly10.config.task.management.extension;

import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ExtensionResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.task.executor.ExtensionsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.management.ManageableServerConfigurationTask;

/**
 * @author emmartins
 */
public abstract class AbstractExtensionSubtask<S> implements ExtensionsManagementSubtaskExecutor<S>, ManageableServerConfigurationTask.Subtasks<S, ManageableServerConfiguration> {

    protected final String extensionModule;

    protected AbstractExtensionSubtask(String extensionModule) {
        this.extensionModule = extensionModule;
    }

    @Override
    public void run(S source, ManageableServerConfiguration configuration, TaskContext parentContext) throws Exception {
        executeSubtasks(source, configuration.getExtensionResources(), parentContext);
    }

    @Override
    public void executeSubtasks(final S source, final ExtensionResources extensionResources, final TaskContext parentContext) throws Exception {
        final ServerMigrationTaskName taskName = getName(source, extensionResources, parentContext);
        if (taskName != null) {
            final ServerMigrationTask task = new AbstractServerMigrationTask(taskName) {
                @Override
                protected ServerMigrationTaskResult runTask(TaskContext context) throws Exception {
                    return AbstractExtensionSubtask.this.runTask(source, extensionResources, context);
                }
            };
            parentContext.execute(task);
        }
    }

    protected abstract ServerMigrationTaskName getName(S source, ExtensionResources extensionResources, TaskContext parentContext);

    protected abstract ServerMigrationTaskResult runTask(S source, ExtensionResources extensionResources, TaskContext context) throws Exception;
}