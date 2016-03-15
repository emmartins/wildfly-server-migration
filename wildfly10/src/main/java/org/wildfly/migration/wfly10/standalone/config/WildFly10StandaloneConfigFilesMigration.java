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
package org.wildfly.migration.wfly10.standalone.config;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerMigrationFailedException;
import org.wildfly.migration.core.ServerPath;
import org.wildfly.migration.core.console.UserConfirmation;
import org.wildfly.migration.wfly10.WildFly10Server;

import java.io.IOException;
import java.util.Collection;

import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Migration of multiple standalone config files.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFilesMigration<S extends Server> {

    private final WildFly10StandaloneConfigFileMigration configFileMigration;

    public WildFly10StandaloneConfigFilesMigration(WildFly10StandaloneConfigFileMigration configFileMigration) {
        this.configFileMigration = configFileMigration;
    }

    public void run(final Collection<ServerPath<S>> sourceConfigs, final WildFly10Server target, final ServerMigrationContext context) throws IOException {
        ROOT_LOGGER.infof("Scanning for standalone server configurations...");
        for (ServerPath standaloneConfig : sourceConfigs) {
            ROOT_LOGGER.infof("%s", standaloneConfig);
        }
        context.getConsoleWrapper().printf("%n");

        if (context.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() {
                    try {
                        confirmAllStandaloneConfigs(sourceConfigs, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onYes() {
                    try {
                        migrateAllStandaloneConfigs(sourceConfigs, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onError() {
                    // repeat
                    try {
                        run(sourceConfigs, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
            };
            new UserConfirmation(context.getConsoleWrapper(), "Migrate all configurations?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
            migrateAllStandaloneConfigs(sourceConfigs, target, context);
        }
    }

    protected void migrateAllStandaloneConfigs(Collection<ServerPath<S>> standaloneConfigs, WildFly10Server target, ServerMigrationContext context) throws IOException {
        for (ServerPath<S> sourceStandaloneConfig : standaloneConfigs) {
            configFileMigration.run(sourceStandaloneConfig, target, context);
        }
    }

    protected void confirmAllStandaloneConfigs(Collection<ServerPath<S>> standaloneConfigs, WildFly10Server target, ServerMigrationContext context) throws IOException {
        for (ServerPath<S> sourceStandaloneConfig : standaloneConfigs) {
            confirmStandaloneConfig(sourceStandaloneConfig, target, context);
        }
    }

    protected void confirmStandaloneConfig(final ServerPath<S> source, final WildFly10Server target, final ServerMigrationContext context) throws IOException {
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
