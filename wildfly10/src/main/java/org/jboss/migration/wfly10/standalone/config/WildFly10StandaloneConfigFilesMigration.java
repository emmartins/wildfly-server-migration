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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.wfly10.WildFly10Server;

import java.util.Collection;

import static org.jboss.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Migration of multiple standalone config files.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFilesMigration<S extends Server> {

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("config-files").build();

    private final WildFly10StandaloneConfigFileMigration configFileMigration;

    public WildFly10StandaloneConfigFilesMigration(WildFly10StandaloneConfigFileMigration configFileMigration) {
        this.configFileMigration = configFileMigration;
    }

    public ServerMigrationTask getServerMigrationTask(final Collection<ServerPath<S>> sourceConfigs, final WildFly10Server target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return getServerMigrationTaskName();
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                WildFly10StandaloneConfigFilesMigration.this.run(sourceConfigs, target, context);
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskName getServerMigrationTaskName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    protected void run(final Collection<ServerPath<S>> sourceConfigs, final WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        taskContext.getLogger().infof("Retrieving source's standalone server config files...");
        for (ServerPath standaloneConfig : sourceConfigs) {
            taskContext.getLogger().infof("%s", standaloneConfig);
        }
        final ServerMigrationContext serverMigrationContext = taskContext.getServerMigrationContext();
        final ConsoleWrapper consoleWrapper = serverMigrationContext.getConsoleWrapper();
        consoleWrapper.printf("%n");
        if (serverMigrationContext.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() throws Exception {
                    confirmAllStandaloneConfigs(sourceConfigs, target, taskContext);
                }
                @Override
                public void onYes() throws Exception {
                    migrateAllStandaloneConfigs(sourceConfigs, target, taskContext);
                }
                @Override
                public void onError() throws Exception {
                    // repeat
                    run(sourceConfigs, target, taskContext);

                }
            };
            new UserConfirmation(consoleWrapper, "Migrate all configurations?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
            migrateAllStandaloneConfigs(sourceConfigs, target, taskContext);
        }
    }

    protected void migrateAllStandaloneConfigs(Collection<ServerPath<S>> standaloneConfigs, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        for (ServerPath<S> sourceStandaloneConfig : standaloneConfigs) {
            taskContext.execute(configFileMigration.getServerMigrationTask(sourceStandaloneConfig, target));
        }
    }

    protected void confirmAllStandaloneConfigs(Collection<ServerPath<S>> standaloneConfigs, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        for (ServerPath<S> sourceStandaloneConfig : standaloneConfigs) {
            confirmStandaloneConfig(sourceStandaloneConfig, target, taskContext);
        }
    }

    protected void confirmStandaloneConfig(final ServerPath<S> source, final WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
            @Override
            public void onNo() throws Exception {
            }
            @Override
            public void onYes() throws Exception {
                taskContext.execute(configFileMigration.getServerMigrationTask(source, target));
            }
            @Override
            public void onError() throws Exception {
                // repeat
                confirmStandaloneConfig(source, target, taskContext);
            }
        };
        final ConsoleWrapper consoleWrapper = taskContext.getServerMigrationContext().getConsoleWrapper();
        consoleWrapper.printf("%n");
        new UserConfirmation(consoleWrapper, "Migrate configuration "+source.getPath()+" ?", ROOT_LOGGER.yesNo(), resultHandler).execute();
    }
}
