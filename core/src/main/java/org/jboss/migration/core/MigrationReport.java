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

/**
 * The migration report.
 * @author emmartins
 */
public class MigrationReport {

    private final Server source;
    private final Server target;
    private final long startTime;
    private final ServerMigrationTaskExecution taskExecution;

    MigrationReport(Server source, Server target, long startTime, ServerMigrationTaskExecution taskExecution) {
        this.source = source;
        this.target = target;
        this.startTime = startTime;
        this.taskExecution = taskExecution;
    }

    /**
     * Retrieves the migration source server.
     * @return the migration source server.
     */
    public Server getSource() {
        return source;
    }

    /**
     * Retrieves the migration target server.
     * @return the migration target server.
     */
    public Server getTarget() {
        return target;
    }

    /**
     * Retrieves the migration start time in milliseconds.
     * @return the migration start time in milliseconds.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the root task execution.
     * @return the root task execution
     */
    public ServerMigrationTaskExecution getTaskExecution() {
        return taskExecution;
    }
}
