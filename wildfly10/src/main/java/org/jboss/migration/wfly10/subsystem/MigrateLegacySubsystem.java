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
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

/**
 * A task which migrates a legacy subsystem.
 * @author emmartins
 */
public class MigrateLegacySubsystem implements WildFly10SubsystemMigrationTask {

    public static final MigrateLegacySubsystem INSTANCE = new MigrateLegacySubsystem();

    private MigrateLegacySubsystem() {
    }

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (!server.getSubsystems().contains(subsystem.getName())) {
            return;
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Migrating subsystem %s...", subsystem.getName());
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()));
        final ModelNode op = Util.createEmptyOperation("migrate", address);
        final ModelNode result = server.getModelControllerClient().execute(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Op result: %s", result.asString());
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new RuntimeException("Subsystem "+subsystem.getName()+" migration failed: "+result.get("migration-error").asString());
        } else {
            final List<String> migrateWarnings = new ArrayList<>();
            if (result.get(RESULT).hasDefined("migration-warnings")) {
                for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                    migrateWarnings.add(modelNode.asString());
                }
            }
            postMigrate(migrateWarnings, server, context);
            if (migrateWarnings.isEmpty()) {
                ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated.", subsystem.getName());
            } else {
                ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated with warnings: %s", subsystem.getName(), migrateWarnings);
            }
            // FIXME tmp workaround for legacy subsystems which do not remove itself
            if (server.getSubsystems().contains(subsystem.getName())) {
                // remove itself after migration
                server.removeSubsystem(subsystem.getName());
                ServerMigrationLogger.ROOT_LOGGER.debugf("Subsystem %s removed after migration.", subsystem.getName());
            }
        }
    }

    protected void postMigrate(List<String> migrationWarnings, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {

    }
}
