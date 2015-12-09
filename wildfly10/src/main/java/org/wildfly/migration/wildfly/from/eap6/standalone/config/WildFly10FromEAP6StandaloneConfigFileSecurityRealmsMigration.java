package org.wildfly.migration.wildfly.from.eap6.standalone.config;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.eap.EAP6StandaloneConfig;
import org.wildfly.migration.wildfly.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration {

    public void run(EAP6StandaloneConfig source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {

        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating security realms...");
        final boolean targetStarted = target.isStarted();
        if (!targetStarted) {
            target.start();
        }
        try {
            for (ModelNode securityRealm : target.getSecurityRealms()) {
                migrateSecurityRealm(securityRealm, source, target, context);
            }
        } finally {
            if (!targetStarted) {
                target.stop();
            }
        }
        ServerMigrationLogger.ROOT_LOGGER.info("Security realms migration done.");
    }

    private void migrateSecurityRealm(ModelNode securityRealm, EAP6StandaloneConfig source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        final Property securityRealmProperty = securityRealm.asProperty();
        final String securityRealmName = securityRealmProperty.getName();
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating security realm: %s", securityRealmName);
        final ModelNode securityRealmValue = securityRealmProperty.getValue();
        if (securityRealmValue.hasDefined(AUTHENTICATION, PROPERTIES)) {
            copyPropertiesFile(securityRealmValue.get(AUTHENTICATION, PROPERTIES), source, target, context);
        }
        if (securityRealmValue.hasDefined(AUTHORIZATION, PROPERTIES)) {
            copyPropertiesFile(securityRealmValue.get(AUTHORIZATION, PROPERTIES), source, target, context);
        }
    }

    private void copyPropertiesFile(ModelNode properties, EAP6StandaloneConfig source, WildFly10StandaloneServer target, ServerMigrationContext context) throws IOException {
        if (properties.hasDefined(PATH)) {
            final String path = properties.get(PATH).asString();
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties path: %s", path);
            String relativeTo = null;
            if (properties.hasDefined(RELATIVE_TO)) {
                relativeTo = properties.get(RELATIVE_TO).asString();
            }
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties relative_to: %s", String.valueOf(relativeTo));
            final Path targetPath;
            if (relativeTo == null) {
                // path is absolute
                targetPath = Paths.get(path);
            } else {
                // path is relative to relative_to
                final Path resolvedPath = target.resolvePath(relativeTo);
                if (resolvedPath == null) {
                    throw new IOException("failed to resolve path "+relativeTo);
                } else {
                    targetPath = resolvedPath.normalize().resolve(path);
                }
            }
            ServerMigrationLogger.ROOT_LOGGER.debugf("Properties file path target: %s", targetPath);
            final Path targetServerBaseDir = target.getServer().getBaseDir();
            if (targetPath.startsWith(targetServerBaseDir)) {
                // properties file resolved to server's base dir, copy
                final Path sourcePath = source.getServer().getBaseDir().resolve(targetServerBaseDir.relativize(targetPath));
                context.getMigrationFiles().copy(sourcePath, targetPath);
            } else {
                // ignore, files not in base dir are not migrated
            }
        }
    }
}