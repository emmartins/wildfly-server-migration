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

/**
 * The server migration task execution.
 * @author emmartins
 */
public class ServerMigrationTaskExecution {

    private final ServerMigrationTask task;
    private final ServerMigrationTaskExecution parent;
    private final List<ServerMigrationTaskExecution> children;
    private final ServerMigrationContext serverMigrationContext;
    private long startTime;
    private volatile ServerMigrationTaskResult result;
    private final Logger logger;

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
        this.logger = Logger.getLogger(ServerMigrationTask.class, task.getId().toString());
    }

    /**
     * Retrieves the task.
     * @return the task.
     */
    ServerMigrationTask getTask() {
        return task;
    }

    /**
     * Retrieves the task logger.
     * @return the task logger
     */
    Logger getLogger() {
        return logger;
    }

    /**
     * Retrieves the task id.
     * @return the task id.
     */
    public ServerMigrationTaskId getTaskId() {
        return task.getId();
    }

    /**
     * Retrieves the absolute task id
     * @return a list with all the task ids
     */
    public List<ServerMigrationTaskId> getAbsoluteTaskId() {
        final ArrayList<ServerMigrationTaskId> result = new ArrayList<>();
        if (parent != null) {
            result.addAll(parent.getAbsoluteTaskId());
        }
        result.add(getTaskId());
        return  result;
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
        final ServerMigrationTaskExecution child = new ServerMigrationTaskExecution(subtask, this);
        children.add(child);
        child.run();
        return child;
    }

    synchronized void run() throws IllegalStateException, ServerMigrationFailedException {
        if (this.result != null) {
            throw new IllegalStateException();
        }
        startTime = System.currentTimeMillis();
        //logger.infof("Task execution starting...");
        try {
            result = task.run(new ServerMigrationTaskContext(this));
        } catch (ServerMigrationFailedException e) {
            result = ServerMigrationTaskResult.fail(e);
            throw e;
        } catch (Throwable e) {
            result = ServerMigrationTaskResult.fail(e);
            throw new ServerMigrationFailedException(e);
        } finally {
            //logger.infof("Task execution completed with result status... %s", result);
        }
    }
}
