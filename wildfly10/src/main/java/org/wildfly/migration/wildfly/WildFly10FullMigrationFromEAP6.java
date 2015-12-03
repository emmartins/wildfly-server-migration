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
package org.wildfly.migration.wildfly;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerMigrationFailedException;
import org.wildfly.migration.wildfly.config.Extension;
import org.wildfly.migration.wildfly.config.Extensions;
import org.wildfly.migration.wildfly.config.Subsystem;
import org.wildfly.migration.wildfly.config.Subsystems;
import org.wildfly.migration.core.console.UserConfirmation;
import org.wildfly.migration.eap.EAP6Server;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wildfly.server.TargetServer;
import org.wildfly.migration.wildfly.server.embedded.EmbeddedStandaloneTargetServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * @author emmartins
 */
public class WildFly10FullMigrationFromEAP6 implements WildFly10FullMigration {

    @Override
    public void migrate(Server source, WildFly10FullServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n");
        ROOT_LOGGER.infof("Executing EAP 6.x --> WildFly Full 10.x migration...");
        context.getConsoleWrapper().printf("%n");
        migrateStandaloneConfigs((EAP6Server) source, target, context);
        context.getConsoleWrapper().printf("%n");
        ROOT_LOGGER.infof("Server migration done.");
    }

    protected void migrateStandaloneConfigs(final EAP6Server source, final WildFly10FullServer target, final ServerMigrationContext context) throws IOException {
        ROOT_LOGGER.infof("Processing standalone server configurations...");
        context.getConsoleWrapper().printf("%n");
        if (context.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() {
                    try {
                        confirmAllStandaloneConfigs(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onYes() {
                    try {
                        migrateAllStandaloneConfigs(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onError() {
                    // repeat
                    try {
                        migrateStandaloneConfigs(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
            };
            new UserConfirmation(context.getConsoleWrapper(), "Migrate all configurations?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
            migrateAllStandaloneConfigs(source, target, context);
        }

    }

    protected void migrateAllStandaloneConfigs(EAP6Server source, WildFly10FullServer target, ServerMigrationContext context) throws IOException {
        for (Path standaloneConfigPath : source.getStandaloneConfigPaths()) {
            migrateStandaloneConfig(standaloneConfigPath, target, context);
        }
    }

    protected void confirmAllStandaloneConfigs(EAP6Server source, WildFly10FullServer target, ServerMigrationContext context) throws IOException {
        for (Path standaloneConfigPath : source.getStandaloneConfigPaths()) {
            confirmStandaloneConfig(standaloneConfigPath, target, context);
        }
    }

    protected void confirmStandaloneConfig(final Path source, final WildFly10FullServer target, final ServerMigrationContext context) throws IOException {
        final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
            @Override
            public void onNo() {
            }
            @Override
            public void onYes() {
                try {
                    migrateStandaloneConfig(source, target, context);
                } catch (IOException e) {
                    throw new ServerMigrationFailedException(e);
                }
            }
            @Override
            public void onError() {
                // repeat
                try {
                    confirmStandaloneConfig(source, target, context);
                } catch (IOException e) {
                    throw new ServerMigrationFailedException(e);
                }
            }
        };
        context.getConsoleWrapper().printf("%n");
        new UserConfirmation(context.getConsoleWrapper(), "Migrate configuration "+source+" ?", ROOT_LOGGER.yesNo(), resultHandler).execute();
    }

    protected void migrateStandaloneConfig(Path source, WildFly10FullServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating standalone server configuration %s", source);
        copyFileToTargetServer(source, target, context);
        final TargetServer targetServer = startEmbeddedServer(source, target, context);
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
        ServerMigrationLogger.ROOT_LOGGER.info("Standalone server configuration file migration done.");
    }

    private static void copyFileToTargetServer(Path source, WildFly10FullServer targetServer, ServerMigrationContext context) throws IOException {
        // check if server file exists
        final Path target = targetServer.getStandaloneConfigurationDir().resolve(source.getFileName());
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

    private static TargetServer startEmbeddedServer(Path source, WildFly10FullServer target, ServerMigrationContext context) throws IOException {
        final String config = source.getFileName().toString();
        ServerMigrationLogger.ROOT_LOGGER.infof("Starting server server configuration %s", config);
        final TargetServer targetServer = new EmbeddedStandaloneTargetServer(config, target.getBaseDir());
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
