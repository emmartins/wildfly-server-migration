/*
 * Copyright 2015 Red Hat, Inc.
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
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.util.List;

/**
 * @author emmartins
 */
public class WildFly10Subsystem {

    private final String name;
    private final WildFly10Extension extension;
    protected final List<WildFly10SubsystemMigrationTaskFactory> subsystemMigrationTasks;
    protected final ServerMigrationTaskId serverMigrationTaskId;

    public WildFly10Subsystem(String name, List<WildFly10SubsystemMigrationTaskFactory> subsystemMigrationTasks, WildFly10Extension extension) {
        this.name = name;
        this.extension = extension;
        this.subsystemMigrationTasks = subsystemMigrationTasks;
        this.serverMigrationTaskId = new ServerMigrationTaskId.Builder()
                .setName("Subsystem Config")
                .addAttribute("name", getName())
                .build();
    }

    public WildFly10Extension getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WildFly10Subsystem subsystem = (WildFly10Subsystem) o;
        return name.equals(subsystem.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public ServerMigrationTask getServerMigrationTask(final WildFly10StandaloneServer server) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskId getId() {
                return serverMigrationTaskId;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (subsystemMigrationTasks != null && !subsystemMigrationTasks.isEmpty()) {
                    final ModelNode subsystemConfig = server.getSubsystem(getName());
                    for (final WildFly10SubsystemMigrationTaskFactory subsystemMigrationTaskFactory : subsystemMigrationTasks) {
                        context.execute(subsystemMigrationTaskFactory.getServerMigrationTask(subsystemConfig, WildFly10Subsystem.this, server));
                    }
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }
}
