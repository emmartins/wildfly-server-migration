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
package org.jboss.migration.wfly10.standalone.config;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Migration logic of WildFly 10 Subsystems, and related Extension.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileSubsystemsMigration<S extends Server> {

    private final List<WildFly10Extension> supportedExtensions;
    private final List<WildFly10Subsystem> supportedSubsystems;

    public WildFly10StandaloneConfigFileSubsystemsMigration(List<WildFly10Extension> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
        this.supportedSubsystems = getSupportedSubsystems(supportedExtensions);
    }

    private static List<WildFly10Subsystem> getSupportedSubsystems(List<WildFly10Extension> supportedExtensions) {
        List<WildFly10Subsystem> supported = new ArrayList<>();
        for (WildFly10Extension extension : supportedExtensions) {
            supported.addAll(extension.getSubsystems());
        }
        return Collections.unmodifiableList(supported);
    }

    public void run(ServerPath<S> source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating subsystems...");
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            final Set<String> subsystems = target.getSubsystems();
            ServerMigrationLogger.ROOT_LOGGER.debugf("Subsystems found: %s", subsystems);
            // delete subsystems/extensions not supported
            removeUnsupportedSubsystems(target, subsystems, context);
            final Set<String> extensions = target.getExtensions();
            ServerMigrationLogger.ROOT_LOGGER.debugf("Extensions found: %s", target.getExtensions());
            removeUnsupportedExtensions(target, extensions, context);
            // migrate extensions/subsystems
            migrateExtensions(target, extensions, context);
        } finally {
            if (!targetStarted) {
                target.stop();
            }
            ServerMigrationLogger.ROOT_LOGGER.info("Subsystems migration done.");
        }
    }

    protected void removeUnsupportedSubsystems(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> subsystems, ServerMigrationContext context) throws IOException {
        for (String subsystem : subsystems) {
            boolean supported = false;
            for (WildFly10Subsystem supportedSubsystem : supportedSubsystems) {
                if (subsystem.equals(supportedSubsystem.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                wildFly10StandaloneServer.removeSubsystem(subsystem);
                ServerMigrationLogger.ROOT_LOGGER.infof("Unsupported subsystem %s removed.", subsystem);
            }
        }
    }

    protected void removeUnsupportedExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensions, ServerMigrationContext context) throws IOException {
        for (String extension : extensions) {
            boolean supported = false;
            for (WildFly10Extension supportedExtension : supportedExtensions) {
                if (extension.equals(supportedExtension.getName())) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                wildFly10StandaloneServer.removeExtension(extension);
                ServerMigrationLogger.ROOT_LOGGER.infof("Unsupported extension %s removed.", extension);
            }
        }
    }

    protected void migrateExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensions, ServerMigrationContext context) throws IOException {
        for (WildFly10Extension supportedExtension : supportedExtensions) {
            //if (extensions.contains(supportedExtension.getName())) {
                supportedExtension.migrate(wildFly10StandaloneServer, context);
            //}
        }
    }
}
