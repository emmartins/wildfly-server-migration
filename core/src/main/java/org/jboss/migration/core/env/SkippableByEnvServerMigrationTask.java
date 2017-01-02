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

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;

/**
 * @author emmartins
 */
public class SkippableByEnvServerMigrationTask implements ServerMigrationTask {
    private final ServerMigrationTask task;
    private final String propertyName;

    public SkippableByEnvServerMigrationTask(ServerMigrationTask task, String propertyName) {
        this.task = task;
        this.propertyName = propertyName;
    }

    public SkippableByEnvServerMigrationTask(ServerMigrationTask task) {
        this(task, task.getName().getName()+".skip");
    }

    @Override
    public ServerMigrationTaskName getName() {
        return task.getName();
    }

    @Override
    public ServerMigrationTaskResult run(final TaskContext context) throws Exception {
        return !context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(propertyName, Boolean.FALSE) ? task.run(context) : ServerMigrationTaskResult.SKIPPED;
    }

    @Override
    public String toString() {
        return "SkippableByEnvServerMigrationTask[task="+task.toString()+", propertyName="+propertyName+"]";
    }
}
