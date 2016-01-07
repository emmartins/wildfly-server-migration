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
package org.wildfly.migration.wfly10.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class LegacyWildFly10Subsystem extends BasicWildFly10Subsystem {

    public LegacyWildFly10Subsystem(String name, WildFly10Extension extension) {
        super(name, extension);
    }

    @Override
    public void migrate(WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        super.migrate(server, context);
        if (!server.getSubsystems().contains(getName())) {
            return;
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Migrating subsystem %s...", getName());
        final PathAddress address = pathAddress(pathElement(SUBSYSTEM, getName()));
        final ModelNode op = Util.createEmptyOperation("migrate", address);
        final ModelNode result = server.getModelControllerClient().execute(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Op result: %s", result.asString());
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new RuntimeException("Subsystem "+getName()+" migration failed: "+result.get("migration-error").asString());
        } else {
            final List<String> migrateWarnings = new ArrayList<>();
            if (result.get(RESULT).hasDefined("migration-warnings")) {
                for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                    migrateWarnings.add(modelNode.asString());
                }
            }
            postMigrate(migrateWarnings, server, context);
            if (migrateWarnings.isEmpty()) {
                ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated.", getName());
            } else {
                ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated with warnings: %s", getName(), migrateWarnings);
            }
            // FIXME tmp workaround for legacy subsystems which do not remove itself
            if (server.getSubsystems().contains(getName())) {
                // remove itself after migration
                server.removeSubsystem(getName());
                ServerMigrationLogger.ROOT_LOGGER.debugf("Subsystem %s removed after migration.", getName());
            }
        }
    }

    protected void postMigrate(List<String> migrationWarnings, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {

    }
}
