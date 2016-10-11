/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.ServerMigrationFailedException;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.console.BasicResultHandlers;
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import java.nio.file.Path;
import java.util.Collection;

import static org.jboss.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Migration of multiple standalone config files.
 * @author emmartins
 */
public class ServerConfigurationsMigration<S extends Server, C, T extends ManageableServerConfiguration> {

    private final ServerMigrationTaskName taskName;
    private final SourceConfigurations<S, C> sourceConfigurations;
    private final ServerConfigurationMigration<C, T> configFileMigration;

    public ServerConfigurationsMigration(SourceConfigurations<S, C> sourceConfigurations, ServerConfigurationMigration<C, T> configFileMigration) {
        this.sourceConfigurations = sourceConfigurations;
        this.configFileMigration = configFileMigration;
        this.taskName = new ServerMigrationTaskName.Builder(configFileMigration.getConfigType()+"-configurations").build();
    }

    public ServerMigrationTask getServerMigrationTask(S source, WildFly10Server target, Path targetConfigDir) {
        return new Task<>(getServerMigrationTaskName(), sourceConfigurations.getConfigurations(source, target), target, targetConfigDir, configFileMigration);
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskName getServerMigrationTaskName() {
        return taskName;
    }

    protected static class Task<S, T extends ManageableServerConfiguration> implements ServerMigrationTask {

        private final ServerMigrationTaskName name;
        private final Collection<S> sourceConfigs;
        private final Path targetConfigDir;
        private final WildFly10Server target;
        private final ServerConfigurationMigration<S, T> configFileMigration;

        protected Task(ServerMigrationTaskName name, Collection<S> sourceConfigs, WildFly10Server target, Path targetConfigDir, ServerConfigurationMigration<S, T> configFileMigration) {
            this.name = name;
            this.sourceConfigs = sourceConfigs;
            this.targetConfigDir = targetConfigDir;
            this.target = target;
            this.configFileMigration = configFileMigration;
        }

        @Override
        public ServerMigrationTaskName getName() {
            return name;
        }

        @Override
        public ServerMigrationTaskResult run(final ServerMigrationTaskContext taskContext) throws Exception {
            final ServerMigrationContext serverMigrationContext = taskContext.getServerMigrationContext();
            final ConsoleWrapper consoleWrapper = serverMigrationContext.getConsoleWrapper();
            consoleWrapper.printf("%n");
            taskContext.getLogger().infof("Retrieving source's %s configurations...", configFileMigration.getConfigType());
            if (!sourceConfigs.isEmpty()) {
                for (S sourceConfig : sourceConfigs) {
                    taskContext.getLogger().infof("%s", sourceConfig);
                }
            } else {
                taskContext.getLogger().infof("No source's %s configurations found.", configFileMigration.getConfigType());
            }

            if (serverMigrationContext.isInteractive()) {
                final BasicResultHandlers.UserConfirmation resultHandler = new BasicResultHandlers.UserConfirmation();
                new UserConfirmation(consoleWrapper, "Migrate all configurations?", ROOT_LOGGER.yesNo(), resultHandler).execute();
                switch (resultHandler.getResult()) {
                    case NO:
                        confirmAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
                        break;
                    case YES:
                        migrateAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
                        break;
                    case ERROR:
                        return run(taskContext);
                    default:
                        throw new ServerMigrationFailedException("unexpected user interaction result");
                }
            } else {
                migrateAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
            }
            return taskContext.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        }

        protected void migrateAllConfigs(Collection<S> sourceConfigs, final Path targetConfigDir, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
            for (S sourceConfig : sourceConfigs) {
                taskContext.execute(configFileMigration.getServerMigrationTask(sourceConfig, targetConfigDir, target));
            }
        }

        protected void confirmAllConfigs(Collection<S> sourceConfigs, final Path targetConfigDir, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
            for (S sourceConfig : sourceConfigs) {
                confirmConfig(sourceConfig, targetConfigDir, target, taskContext);
            }
        }

        protected void confirmConfig(final S sourceConfig, final Path targetConfigDir, final WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() throws Exception {
                }
                @Override
                public void onYes() throws Exception {
                    taskContext.execute(configFileMigration.getServerMigrationTask(sourceConfig, targetConfigDir, target));
                }
                @Override
                public void onError() throws Exception {
                    // repeat
                    confirmConfig(sourceConfig, targetConfigDir, target, taskContext);
                }
            };
            final ConsoleWrapper consoleWrapper = taskContext.getServerMigrationContext().getConsoleWrapper();
            new UserConfirmation(consoleWrapper, "Migrate configuration "+sourceConfig+" ?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        }
    }

    /**
     * Component responsible for providing the source server's configurations.
     * @param <S> the source server type
     * @param <C> the source configuration type
     */
    public interface SourceConfigurations<S extends Server, C> {
        Collection<C> getConfigurations(S source, WildFly10Server target);
    }
}
