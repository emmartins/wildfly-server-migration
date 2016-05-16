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
package org.jboss.migration.wfly10.standalone;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.WildFly10Server;

import java.util.List;

/**
 * Abstract implementation for the standalone server migration.
 * @author emmartins
 */
public abstract class WildFly10StandaloneServerMigration<S extends Server> {

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("standalone-server-migration").build();

    /**
     *
     * @param source
     * @param target
     * @return
     */
    public ServerMigrationTask getServerMigrationTask(final S source, final WildFly10Server target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return getServerMigrationTaskId();
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                return WildFly10StandaloneServerMigration.this.run(source, target, context);
            }
        };
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskId getServerMigrationTaskId() {
        return SERVER_MIGRATION_TASK_ID;
    }

    /**
     *
     * @param source
     * @param target
     * @param context
     * @return
     */
    protected ServerMigrationTaskResult run(final S source, WildFly10Server target, ServerMigrationTaskContext context) {
        final List<ServerMigrationTask> subtasks = getSubtasks(source, target, context);
        if (subtasks != null) {
            for (ServerMigrationTask subtask : subtasks) {
                context.execute(subtask);
            }
        }
        return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    /**
     *
     * @param source
     * @param target
     * @param context
     * @return
     */
    protected abstract List<ServerMigrationTask> getSubtasks(S source, WildFly10Server target, ServerMigrationTaskContext context);
}
