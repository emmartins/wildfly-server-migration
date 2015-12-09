package org.wildfly.migration.wildfly.from.eap6.standalone.config;

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.eap.EAP6StandaloneConfig;
import org.wildfly.migration.wildfly.standalone.WildFly10StandaloneServer;
import org.wildfly.migration.wildfly.standalone.config.Extension;
import org.wildfly.migration.wildfly.standalone.config.Subsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration {

    private final List<Extension> supportedExtensions;
    private final List<Subsystem> supportedSubsystems;

    public WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration(List<Extension> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
        this.supportedSubsystems = getSupportedSubsystems(supportedExtensions);
    }

    private static List<Subsystem> getSupportedSubsystems(List<Extension> supportedExtensions) {
        List<Subsystem> supported = new ArrayList<>();
        for (Extension extension : supportedExtensions) {
            supported.addAll(extension.getSubsystems());
        }
        return Collections.unmodifiableList(supported);
    }

    public void run(EAP6StandaloneConfig source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            final Set<String> subsystems = target.getSubsystems();
            ServerMigrationLogger.ROOT_LOGGER.infof("Subsystems found: %s", subsystems);
            // delete subsystems/extensions not supported
            removeUnsupportedSubsystems(target, subsystems);
            final Set<String> extensions = target.getExtensions();
            ServerMigrationLogger.ROOT_LOGGER.infof("Extensions found: %s", target.getExtensions());
            removeUnsupportedExtensions(target, extensions);
            // migrate subsystems
            migrateSubsystems(target, subsystems);
        } finally {
            if (!targetStarted) {
                target.stop();
            }
        }
    }

    private void removeUnsupportedSubsystems(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> subsystems) throws IOException {
        for (String subsystem : subsystems) {
            boolean supported = false;
            for (Subsystem supportedSubsystem : supportedSubsystems) {
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

    private void removeUnsupportedExtensions(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> extensions) throws IOException {
        for (String extension : extensions) {
            boolean supported = false;
            for (Extension supportedExtension : supportedExtensions) {
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

    private void migrateSubsystems(WildFly10StandaloneServer wildFly10StandaloneServer, Set<String> subsystems) throws IOException {
        for (String subsystem : subsystems) {
            for (Subsystem supportedSubsystem : supportedSubsystems) {
                if (subsystem.equals(supportedSubsystem.getName()) && supportedSubsystem.isMigrationRequired()) {
                    wildFly10StandaloneServer.migrateSubsystem(subsystem);
                    ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s migrated.", subsystem);
                    break;
                }
            }
        }
    }
}
