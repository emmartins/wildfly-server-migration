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

package org.jboss.migration.core.env;

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;

/**
 * @author emmartins
 */
public class TaskEnvironment extends SubEnvironment {

    private static final String PROPERTY_SKIP = "skip";

    public TaskEnvironment(Environment environment, String propertyNamesBase) {
        super(environment, propertyNamesBase+".");
    }

    public TaskEnvironment(Environment environment, ServerMigrationTaskName taskName) {
        this(environment, taskName.getName());
    }

    public TaskEnvironment(TaskContext taskContext) {
        this(taskContext.getMigrationEnvironment(), taskContext.getTaskName());
    }

    public TaskEnvironment(Environment environment, ServerMigrationTaskName taskName, ServerMigrationTaskName subtaskName) {
        this(environment, taskName.getName()+"."+subtaskName.getName());
    }

    public boolean isSkippedByEnvironment() {
        return getPropertyAsBoolean(PROPERTY_SKIP, Boolean.FALSE);
    }
}
