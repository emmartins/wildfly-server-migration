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

package org.jboss.migration.core.console;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.logger.ServerMigrationLogger;

/**
 * @author emmartins
 */
public class UserConfirmationServerMigrationTask implements ServerMigrationTask {

    private final ServerMigrationTask task;
    private final String message;

    public UserConfirmationServerMigrationTask(ServerMigrationTask task, String message) {
        this.task = task;
        this.message = message;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return task.getName();
    }

    protected ServerMigrationTaskResult confirmTaskRun(final TaskContext context) throws Exception {
        final BasicResultHandlers.UserConfirmation resultHandler = new BasicResultHandlers.UserConfirmation();
        new UserConfirmation(context.getServerMigrationContext().getConsoleWrapper(), message, ServerMigrationLogger.ROOT_LOGGER.yesNo(), resultHandler).execute();
        switch (resultHandler.getResult()) {
            case NO:
                return ServerMigrationTaskResult.SKIPPED;
            case YES:
                return runTask(context);
            case ERROR:
            default:
                return confirmTaskRun(context);
        }
    }

    protected ServerMigrationTaskResult runTask(final TaskContext context) throws Exception {
        return task.run(context);
    }

    @Override
    public ServerMigrationTaskResult run(final TaskContext context) throws Exception {
        return context.getServerMigrationContext().isInteractive() ? confirmTaskRun(context) : runTask(context);
    }
}
