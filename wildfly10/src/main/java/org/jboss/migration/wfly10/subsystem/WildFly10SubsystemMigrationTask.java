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
package org.jboss.migration.wfly10.subsystem;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

/**
 * Abstract implementation for a subsystem config migration task.
 * @author emmartins
 */
public abstract class WildFly10SubsystemMigrationTask implements ServerMigrationTask {
    private final ModelNode config;
    private final WildFly10Subsystem subsystem;
    private final WildFly10StandaloneServer server;

    protected WildFly10SubsystemMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        this.config = config;
        this.subsystem = subsystem;
        this.server = server;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem.getName(), this.getName().getName()));
        // check if subtask was skipped by env
        if (taskEnvironment.isSkippedByEnvironment()) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        return run(config, subsystem, server, context, taskEnvironment);
    }

    protected abstract ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception;
}