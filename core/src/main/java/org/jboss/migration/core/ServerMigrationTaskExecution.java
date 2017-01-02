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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The server migration task execution.
 * @author emmartins
 */
public class ServerMigrationTaskExecution {

    private static final AtomicLong taskCounter = new AtomicLong(0);

    private final ServerMigrationTask task;
    private final ServerMigrationTaskExecution parent;
    private final List<ServerMigrationTaskExecution> children;
    private final ServerMigrationContext serverMigrationContext;
    private long startTime;
    private volatile ServerMigrationTaskResult result;
    private final Logger logger;
    private final long taskNumber;
    private final ServerMigrationTaskPath taskPath;

    ServerMigrationTaskExecution(ServerMigrationTask task, ServerMigrationTaskExecution parent) {
        this(task, parent, parent.serverMigrationContext);
    }

    ServerMigrationTaskExecution(ServerMigrationTask task, ServerMigrationContext serverMigrationContext) {
        this(task, null, serverMigrationContext);
    }

    private ServerMigrationTaskExecution(ServerMigrationTask task, ServerMigrationTaskExecution parent, ServerMigrationContext serverMigrationContext) {
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
        return startTime;
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
    public List<ServerMigrationTaskExecution> getSubtasks() {
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
    ServerMigrationContext getServerMigrationContext() {
        return serverMigrationContext;
    }

    /**
     * Retrieves the parent task execution.
     * @return the parent task execution
     */
    ServerMigrationTaskExecution getParent() {
        return parent;
    }

    /**
     * Executes a subtask
     * @param subtask the subtask to execute
     * @return the subtask execution
     * @throws IllegalStateException if the task result is already set
     * @throws ServerMigrationFailedException if the subtask execution failed
     */
    ServerMigrationTaskExecution execute(ServerMigrationTask subtask) throws IllegalStateException, ServerMigrationFailedException {
        if (this.result != null) {
            throw new IllegalStateException();
        }
        if (subtask.getName() == null) {
            throw new IllegalArgumentException("substask "+subtask+" has no name");
        }
        final ServerMigrationTaskExecution child = new ServerMigrationTaskExecution(subtask, this);
        children.add(child);
        child.run();
        return child;
    }

    synchronized void run() throws IllegalStateException, ServerMigrationFailedException {
        if (this.result != null) {
            throw new IllegalStateException("Task "+ taskPath +" already run");
        }
        startTime = System.currentTimeMillis();
        logger.debugf("Task %s execution starting...", taskPath);
        try {
            result = task.run(new TaskContextImpl(this));
        } catch (ServerMigrationFailedException e) {
            result = ServerMigrationTaskResult.fail(e);
            throw e;
        } catch (Throwable t) {
            final ServerMigrationFailedException e = new ServerMigrationFailedException(t);
            result = ServerMigrationTaskResult.fail(e);
            throw e;
        } finally {
            logger.debugf("Task %s execution completed with result status... %s", taskPath, result);
        }
    }
}
