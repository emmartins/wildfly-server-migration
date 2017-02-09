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
package org.jboss.migration.core.task;

import org.jboss.logging.Logger;
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.component.TaskRunnable;

import java.util.List;

/**
 * The task's context impl.
 * @author emmartins
 */
public class TaskContextImpl implements TaskContext {

    private final TaskExecutionImpl taskExecution;

    TaskContextImpl(TaskExecutionImpl taskExecution) {
        this.taskExecution = taskExecution;
    }

    @Override
    public ServerMigrationTaskName getTaskName() {
        return taskExecution.getTaskName();
    }

    @Override
    public TaskExecution getParentTask() {
        return taskExecution.getParent();
    }

    @Override
    public List<? extends TaskExecution> getSubtasks() {
        return taskExecution.getSubtasks();
    }

    @Override
    public boolean hasSucessfulSubtasks() {
        for (TaskExecution subtask : getSubtasks()) {
            final ServerMigrationTaskResult result = subtask.getResult();
            if (result != null && result.getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TaskExecutionImpl execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailureException {
        return taskExecution.execute(subtask);
    }

    @Override
    public TaskExecution execute(ServerMigrationTaskName taskName, TaskRunnable taskRunnable) throws IllegalStateException, ServerMigrationFailureException {
        return execute(new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }
            @Override
            public ServerMigrationTaskResult run(TaskContext context) throws ServerMigrationFailureException {
                return taskRunnable.run(context);
            }
        });
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
