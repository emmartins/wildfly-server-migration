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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.CompositeSubtasksBuilder;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.WildFlyServerMigration10;

/**
 * @author emmartins
 */
public class CompositeServerMigration<S extends Server> implements WildFlyServerMigration10<S> {

    protected final CompositeSubtasksBuilder<ServerMigrationParameters<S>, ?> subtasks;

    public CompositeServerMigration(CompositeSubtasksBuilder<ServerMigrationParameters<S>, ?> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public TaskRunnable build(ServerMigrationParameters<S> params, ServerMigrationTaskName taskName) {
        return context -> subtasks.build(params, context.getTaskName()).run(context);
    }
}
