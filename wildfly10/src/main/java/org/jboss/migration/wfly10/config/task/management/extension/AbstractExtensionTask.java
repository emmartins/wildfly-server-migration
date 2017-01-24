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
import org.jboss.migration.wfly10.config.management.ExtensionConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;

import java.util.Collection;
import java.util.Set;

/**
 * @author emmartins
 */
public abstract class AbstractExtensionTask<S> implements ManageableResourceTask.SubtaskExecutor<S, ManageableResource> {

    protected final String extensionModule;

    protected AbstractExtensionTask(String extensionModule) {
        this.extensionModule = extensionModule;
    }

    @Override
    public void run(S source, Collection<? extends ManageableResource> resources, TaskContext context) throws Exception {
        final Set<ExtensionConfiguration.Parent> parents = ManageableResourceSelectors.toServerConfiguration().andThen(ManageableResourceSelectors.selectResources(ExtensionConfiguration.Parent.class)).collect(resources);
        for (ExtensionConfiguration.Parent parent : parents) {
            final ServerMigrationTaskName taskName = getName(source, parent, context);
            if (taskName != null) {
                final ServerMigrationTask task = new AbstractServerMigrationTask(taskName) {
                    @Override
                    protected ServerMigrationTaskResult runTask(TaskContext context) throws Exception {
                        return AbstractExtensionTask.this.runTask(source, parent, context);
                    }
                };
                context.execute(task);
            }
        }
    }

    protected abstract ServerMigrationTaskName getName(S source, ExtensionConfiguration.Parent extensionResourceParent, TaskContext parentContext);

    protected abstract ServerMigrationTaskResult runTask(S source, ExtensionConfiguration.Parent extensionResourceParent, TaskContext context) throws Exception;
}