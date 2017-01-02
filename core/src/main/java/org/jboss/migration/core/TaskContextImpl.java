/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
 * The task's context impl.
 * @author emmartins
 */
public class TaskContextImpl implements TaskContext {

    private final ServerMigrationTaskExecution taskExecution;

    TaskContextImpl(ServerMigrationTaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    @Override
    public List<? extends ServerMigrationTaskExecution> getSubtasks() {
        return taskExecution.getSubtasks();
    }

    @Override
    public boolean hasSucessfulSubtasks() {
        for (ServerMigrationTaskExecution subtask : getSubtasks()) {
            final ServerMigrationTaskResult result = subtask.getResult();
            if (result != null && result.getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ServerMigrationTaskExecution execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailedException {
        return taskExecution.execute(subtask);
    }

    @Override
    public ServerMigrationContext getServerMigrationContext() {
        return taskExecution.getServerMigrationContext();
    }

    @Override
    public Logger getLogger() {
        return taskExecution.getLogger();
    }
}
