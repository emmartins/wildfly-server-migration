package org.wildfly.migration.wfly10.from.eap6.standalone;

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerMigrationFailedException;
import org.wildfly.migration.core.console.UserConfirmation;
import org.wildfly.migration.eap.EAP6Server;
import org.wildfly.migration.eap.EAP6StandaloneConfig;
import org.wildfly.migration.wfly10.WildFly10Server;
import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileMigration;

import java.io.IOException;

import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * @author emmartins
 */
public class WildFly10FromEAP6StandaloneMigration {

    private final WildFly10FromEAP6StandaloneConfigFileMigration configFileMigration;

    public WildFly10FromEAP6StandaloneMigration(WildFly10FromEAP6StandaloneConfigFileMigration configFileMigration) {
        this.configFileMigration = configFileMigration;
    }

    public void run(final EAP6Server source, final WildFly10Server target, final ServerMigrationContext context) throws IOException {
        ROOT_LOGGER.infof("Scanning for standalone server configurations...");
        for (EAP6StandaloneConfig standaloneConfig : source.getStandaloneConfigs()) {
            ROOT_LOGGER.infof("%s", standaloneConfig);
        }
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
                        run(source, target, context);
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

    protected void migrateAllStandaloneConfigs(EAP6Server source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        for (EAP6StandaloneConfig sourceStandaloneConfig : source.getStandaloneConfigs()) {
            configFileMigration.run(sourceStandaloneConfig, target, context);
        }
    }

    protected void confirmAllStandaloneConfigs(EAP6Server source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        for (EAP6StandaloneConfig sourceStandaloneConfig : source.getStandaloneConfigs()) {
            confirmStandaloneConfig(sourceStandaloneConfig, target, context);
        }
    }

    protected void confirmStandaloneConfig(final EAP6StandaloneConfig source, final WildFly10Server target, final ServerMigrationContext context) throws IOException {
        final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
            @Override
            public void onNo() {
            }
            @Override
            public void onYes() {
                try {
                    configFileMigration.run(source, target, context);
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
        new UserConfirmation(context.getConsoleWrapper(), "Migrate configuration "+source.getPath()+" ?", ROOT_LOGGER.yesNo(), resultHandler).execute();
    }
}
