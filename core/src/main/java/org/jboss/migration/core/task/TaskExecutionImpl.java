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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The server migration task execution.
 * @author emmartins
 */
public class TaskExecutionImpl implements TaskExecution {

    private static final AtomicLong taskCounter = new AtomicLong(0);

    private final ServerMigrationTask task;
    private final TaskExecutionImpl parent;
    private final List<TaskExecutionImpl> children;
    private final ServerMigrationContext serverMigrationContext;
    private final AtomicLong startTime = new AtomicLong(0L);
    private ServerMigrationTaskResult result;
    private final Logger logger;
    private final long taskNumber;
    private final ServerMigrationTaskPath taskPath;

    public TaskExecutionImpl(ServerMigrationTask task, TaskExecutionImpl parent) {
        this(task, parent, parent.serverMigrationContext);
    }

    public TaskExecutionImpl(ServerMigrationTask task, ServerMigrationContext serverMigrationContext) {
        this(task, null, serverMigrationContext);
    }

    private TaskExecutionImpl(ServerMigrationTask task, TaskExecutionImpl parent, ServerMigrationContext serverMigrationContext) {
        this.task = task;
        this.parent = parent;
        this.serverMigrationContext = serverMigrationContext;
        this.children = new ArrayList<>();
        taskNumber = taskCounter.incrementAndGet();
        this.logger = Logger.getLogger(ServerMigrationTask.class.getName()+'#'+String.valueOf(taskNumber));
        this.taskPath = new ServerMigrationTaskPath(task.getName(), parent != null ? parent.getTaskPath() : null);
    }

    /**
     * Retrieves the task.
     * @return the task.
     */
    ServerMigrationTask getTask() {
        return task;
    }

    /**
     * Retrieves the task number.
     * @return the task number
     */
    public long getTaskNumber() {
        return taskNumber;
    }

    /**
     * Retrieves the task logger.
     * @return the task logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Retrieves the task name.
     * @return the task name.
     */
    public ServerMigrationTaskName getTaskName() {
        return task.getName();
    }

    /**
     * Retrieves the task execution start time in milliseconds.
     * @return
     */
    public long getStartTime() {
        return startTime.get();
    }

    /**
     * Retrieves the task's path
     * @return the task's path
     */
    public ServerMigrationTaskPath getTaskPath() {
        return taskPath;
    }

    /**
     * Retrieves the children task executions.
     * @return the children task executions
     */
    public List<TaskExecution> getSubtasks() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Retrieves the task result.
     * @return  the task result
     */
    public ServerMigrationTaskResult getResult() {
        return result;
    }

    /**
     * Retrieves the server migration context.
     * @return the server migration context
     */
    public ServerMigrationContext getServerMigrationContext() {
        return serverMigrationContext;
    }

    /**
     * Retrieves the parent task execution.
     * @return the parent task execution
     */
    TaskExecutionImpl getParent() {
        return parent;
    }

    /**
     * Executes a subtask
     * @param subtask the subtask to execute
     * @return the subtask execution
     * @throws IllegalStateException if the task result is already set
     * @throws ServerMigrationFailureException if the subtask execution failed
     */
    TaskExecutionImpl execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailureException {
        if (this.result != null) {
            throw new IllegalStateException();
        }
        if (subtask.getName() == null) {
            throw new IllegalArgumentException("substask "+subtask+" has no name");
        }
        final TaskExecutionImpl child = new TaskExecutionImpl(subtask, this);
        children.add(child);
        child.run();
        return child;
    }

    public void run() throws IllegalStateException, ServerMigrationFailureException {
        if (!startTime.compareAndSet(0L, System.currentTimeMillis())) {
            throw new IllegalStateException("Task "+ taskPath +" already started");
        }
        logger.debugf("Task %s execution starting...", taskPath);
        try {
            result = task.run(new TaskContextImpl(this));
        } catch (ServerMigrationFailureException e) {
            throw (result = ServerMigrationTaskResult.fail(e)).getFailReason();
        } catch (Throwable t) {
            throw (result = ServerMigrationTaskResult.fail(new ServerMigrationFailureException(t))).getFailReason();
        } finally {
            logger.debugf("Task %s execution completed with result status... %s", taskPath, result);
        }
    }
}
