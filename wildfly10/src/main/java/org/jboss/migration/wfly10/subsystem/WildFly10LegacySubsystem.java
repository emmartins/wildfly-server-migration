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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

/**
 * A legacy deprecated subsystem, the server only includes support to load and migrate its configuration to the subsystem which replaces it.
 * @author emmartins
 */
public class WildFly10LegacySubsystem extends WildFly10Subsystem {

    public WildFly10LegacySubsystem(String name, WildFly10Extension extension) {
        super(name, "migrate-subsystem", null, extension);
    }

    /**
     * Post migration processing.
     * @param migrationWarnings the warnings that resulted from doing the migration
     * @param server the server
     * @param context the task context
     * @throws Exception if there was a failure processing the warnings
     */
    protected void processWarnings(List<String> migrationWarnings, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(final WildFly10StandaloneServer server) {
        final String subsystemName = getName();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return serverMigrationTaskName;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final ModelNode subsystemConfig = server.getSubsystem(subsystemName);
                if (subsystemConfig == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().debugf("Migrating subsystem %s...", subsystemName);
                final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystemName));
                final ModelNode op = Util.createEmptyOperation("migrate", address);
                final ModelNode result = server.getModelControllerClient().execute(op);
                context.getLogger().debugf("Op result: %s", result.asString());
                final String outcome = result.get(OUTCOME).asString();
                if(!SUCCESS.equals(outcome)) {
                    throw new RuntimeException("Subsystem "+subsystemName+" migration failed: "+result.get("migration-error").asString());
                } else {
                    final List<String> migrateWarnings = new ArrayList<>();
                    if (result.get(RESULT).hasDefined("migration-warnings")) {
                        for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                            migrateWarnings.add(modelNode.asString());
                        }
                    }
                    processWarnings(migrateWarnings, server, context);
                    if (migrateWarnings.isEmpty()) {
                        context.getLogger().infof("Subsystem %s migrated.", subsystemName);
                    } else {
                        context.getLogger().infof("Subsystem %s migrated with warnings: %s", subsystemName, migrateWarnings);
                    }
                    // FIXME tmp workaround for legacy subsystems which do not remove itself
                    if (server.getSubsystems().contains(subsystemName)) {
                        // remove itself after migration
                        server.removeSubsystem(subsystemName);
                        context.getLogger().debugf("Subsystem %s removed after migration.", subsystemName);
                    }
                    return ServerMigrationTaskResult.SUCCESS;
                }
            }
        };
    }
}
