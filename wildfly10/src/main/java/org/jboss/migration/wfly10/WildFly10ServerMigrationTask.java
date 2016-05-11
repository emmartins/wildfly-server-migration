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
package org.jboss.migration.wfly10;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServerMigration;

/**
 * Abstract implementation of the root WFLY 10 server migration task.
 * @author emmartins
 */
public class WildFly10ServerMigrationTask<S extends Server> implements ServerMigrationTask {

    private final S source;
    private final WildFly10Server target;
    private final ServerMigrationTaskId taskId;
    private final WildFly10StandaloneServerMigration standaloneServerMigration;

    public WildFly10ServerMigrationTask(S source, WildFly10Server target, WildFly10StandaloneServerMigration standaloneServerMigration) {
        this.source = source;
        this.target = target;
        this.taskId = new ServerMigrationTaskId.Builder().setName("Migrate Server")
                .addAttribute("source", source.getProductInfo().toString())
                .addAttribute("target",target.getProductInfo().toString())
                .build();
        this.standaloneServerMigration = standaloneServerMigration;
    }

    @Override
    public ServerMigrationTaskId getId() {
        return taskId;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        context.getServerMigrationContext().getConsoleWrapper().printf("Server migration starting...%n");
        try {
            return run(source, target, context);
        } finally {
            context.getServerMigrationContext().getConsoleWrapper().printf("Server migration done.%n");
        }
    }

    /**
     *
     * @param source
     * @param target
     * @param context
     * @return
     * @throws Exception
     */
    protected ServerMigrationTaskResult run(S source, WildFly10Server target, ServerMigrationTaskContext context) throws Exception {
        context.execute(standaloneServerMigration.getServerMigrationTask(source, target));
        return ServerMigrationTaskResult.SUCCESS;
    }
}
