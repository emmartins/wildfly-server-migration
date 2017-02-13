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
 * The task's context.
 * @author emmartins
 */
public interface TaskContext extends ServerMigrationContext {

    /**
     * Retrieves the name of the task in context.
     * @return the name of the task in context
     */
    ServerMigrationTaskName getTaskName();

    /**
     * Retrieves the task's parent.
     * @return the task's parent, if exists, otherwise null.
     */
    TaskExecution getParentTask();

    /**
     * Retrieves the children task executions.
     * @return the children task executions
     */
    List<? extends TaskExecution> getSubtasks();

    /**
     * Indicates if there is any subtask that had success status
     * @return true if there is at least one subtask that had success status; false otherwise
     */
    boolean hasSucessfulSubtasks();

    /**
     * Executes a subtask
     * @param subtask the subtask to execute
     * @return the subtask execution
     * @throws IllegalStateException if the task result is already set
     * @throws ServerMigrationFailureException if the subtask execution failed
     */
    TaskExecution execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailureException;

    /**
     * Creates and executes a subtask, from the specified name and runnable components.
     * @param taskName the subtask's name
     * @param taskRunnable the subtask's runnable
     * @return the
     * @throws IllegalStateException
     * @throws ServerMigrationFailureException
     */
    TaskExecution execute(ServerMigrationTaskName taskName, TaskRunnable taskRunnable) throws IllegalStateException, ServerMigrationFailureException;

    /**
     * Retrieves the task logger.
     * @return the task logger
     */
    Logger getLogger();
}
