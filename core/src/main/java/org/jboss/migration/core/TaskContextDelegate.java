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
package org.jboss.migration.core;

import org.jboss.logging.Logger;

import java.util.List;

/**
 * A task context delegate.
 * @author emmartins
 */
public class TaskContextDelegate implements TaskContext {

    private final TaskContext taskContext;

    public TaskContextDelegate(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public List<? extends ServerMigrationTaskExecution> getSubtasks() {
        return taskContext.getSubtasks();
    }

    @Override
    public boolean hasSucessfulSubtasks() {
        return taskContext.hasSucessfulSubtasks();
    }

    @Override
    public ServerMigrationTaskExecution execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailedException {
        return taskContext.execute(subtask);
    }

    @Override
    public ServerMigrationContext getServerMigrationContext() {
        return taskContext.getServerMigrationContext();
    }

    @Override
    public Logger getLogger() {
        return taskContext.getLogger();
    }
}
