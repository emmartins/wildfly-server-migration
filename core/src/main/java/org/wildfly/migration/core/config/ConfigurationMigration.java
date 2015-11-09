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
package org.wildfly.migration.core.config;

import org.wildfly.migration.core.MigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.core.server.TargetServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * @author emmartins
  */
public class ConfigurationMigration {

    public static void run(Path source, MigrationContext context) throws IOException {
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating standalone server configuration file %s", source);
        copyFileToTargetServer(source, context);
        final TargetServer targetServer = startEmbeddedServer(source, context);
        final Set<String> subsystems = targetServer.getManagementClient().getSubsystems();
        ServerMigrationLogger.ROOT_LOGGER.infof("Subsystems found: %s", subsystems);
        // delete subsystems/extensions not supported
        removeUnsupportedSubsystems(targetServer, subsystems);
        final Set<String> extensions = targetServer.getManagementClient().getExtensions();
        ServerMigrationLogger.ROOT_LOGGER.infof("Extensions found: %s", targetServer.getManagementClient().getExtensions());
        removeUnsupportedExtensions(targetServer, extensions);
        // migrate subsystems
        migrateSubsystems(targetServer, subsystems);
        // shutdown server
        targetServer.stop();
    }

    private static void copyFileToTargetServer(Path source, MigrationContext context) throws IOException {
        // check if server file exists
        final Path target = context.getTargetServerFactory().getServerPaths().getStandaloneConfigurationDir().resolve(source.getFileName());
        ServerMigrationLogger.ROOT_LOGGER.debugf("Source server configuration file is %s", source);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Target server configuration file is %s", target);
        if (Files.exists(target)) {
            ServerMigrationLogger.ROOT_LOGGER.infof("Target server configuration file %s exists, renaming to %s.beforeMigration", target, target.getFileName().toString());
            Files.copy(target, target.getParent().resolve(target.getFileName().toString()+".beforeMigration"), StandardCopyOption.REPLACE_EXISTING);
        }
        // copy file
        ServerMigrationLogger.ROOT_LOGGER.tracef("Copying server configuration file %s", target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        ServerMigrationLogger.ROOT_LOGGER.infof("Server configuration file %s copied to %s", source, target);
    }

    private static TargetServer startEmbeddedServer(Path source, MigrationContext context) throws IOException {
        final String config = source.getFileName().toString();
        ServerMigrationLogger.ROOT_LOGGER.infof("Starting server server configuration %s", config);
        final TargetServer targetServer = context.getTargetServerFactory().newStandaloneTargetServer(config);
        targetServer.start();
        ServerMigrationLogger.ROOT_LOGGER.infof("Started server server configuration %s", config);
        return targetServer;
    }

    private static void removeUnsupportedSubsystems(TargetServer targetServer, Set<String> subsystems) throws IOException {
        for (String subsystem : subsystems) {
            boolean supported = false;
            for (Subsystem supportedSubsystem : Subsystems.SUPPORTED) {
                if (subsystem.equals(supportedSubsystem.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                targetServer.getManagementClient().removeSubsystem(subsystem);
                ServerMigrationLogger.ROOT_LOGGER.infof("Unsupported subsystem %s removed.", subsystem);
            }
        }
    }

    private static void removeUnsupportedExtensions(TargetServer targetServer, Set<String> extensions) throws IOException {
        for (String extension : extensions) {
            boolean supported = false;
            for (Extension supportedExtension : Extensions.SUPPORTED) {
                if (extension.equals(supportedExtension.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                targetServer.getManagementClient().removeExtension(extension);
                ServerMigrationLogger.ROOT_LOGGER.infof("Unsupported extension %s removed.", extension);
            }
        }
    }

    private static void migrateSubsystems(TargetServer targetServer, Set<String> subsystems) throws IOException {
        for (String subsystem : subsystems) {
            for (Subsystem supportedSubsystem : Subsystems.SUPPORTED) {
                if (subsystem.equals(supportedSubsystem.getName()) && supportedSubsystem.isMigrationRequired()) {
                    targetServer.getManagementClient().migrateSubsystem(subsystem);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated.", subsystem);
                    break;
                }
            }
        }
    }

}
