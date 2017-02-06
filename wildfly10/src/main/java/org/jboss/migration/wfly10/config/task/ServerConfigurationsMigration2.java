/*
 * Copyright 2017 Red Hat, Inc.
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
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.ComponentTask;
import org.jboss.migration.core.task.component.CompositeSubtasks;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.wfly10.WildFlyServer10;

import java.util.Collection;

/**
 * Domain migration task implementation.
 * @author emmartins
 */
public class ServerConfigurationsMigration2 extends ComponentTask {

    protected ServerConfigurationsMigration2(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    /**
     * Base abstract builder.
     * @param <S> the source server type
     * @param <P> the server migration build params
     * @param <T> the type of the concrete builder which extends this builder
     */
    protected static abstract class BaseBuilder<S extends Server, C, P extends ServerMigrationParameters<S>, T extends BaseBuilder<S, C, P, T>> extends ComponentTask.Builder<P, T> {

        private SourceConfigurations<S, C> sourceConfigurations;
        private ServerConfigurationMigration<C, ?> configFileMigration;

        protected BaseBuilder() {
            beforeRun(context -> {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n");
                context.getLogger().infof("Domain migration starting...");
            });
            afterRun(context -> {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n");
                context.getLogger().infof("Domain migration done.");
            });
        }

        @Override
        protected ServerMigrationTaskName buildName(P parameters) {
            ServerMigrationTaskName taskName = super.buildName(parameters);
            if (taskName == null) {
                taskName = new ServerMigrationTaskName.Builder(configFileMigration.getConfigType()+"-configurations").build();
            }
            return taskName;
        }

        public T sourceConfigurations(SourceConfigurations<S, C> sourceConfigurations) {
            this.sourceConfigurations = sourceConfigurations;
            return getThis();
        }

        public T configFileMigration(ServerConfigurationMigration<C, ?> configFileMigration) {
            this.configFileMigration = configFileMigration;
            return getThis();
        }

        @Override
        protected TaskRunnable.Builder<? super P> getRunnableBuilder() {
            final CompositeSubtasks.Builder subtasksBuilder = new CompositeSubtasks.Builder<>();
            if (domainConfigurationsMigration != null) {
                subtasksBuilder.subtask(domainConfigurationsMigration);
            }
            if (hostConfigurationsMigration != null) {
                subtasksBuilder.subtask(hostConfigurationsMigration);
            }
            return subtasksBuilder;
        }
    }

    /**
     * A concrete builder implementation.
     * @param <S> the source server type
     */
    public static class Builder<S extends Server> extends BaseBuilder<S, ServerMigrationParameters<S>, Builder<S>> {

        @Override
        protected Builder<S> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new ServerConfigurationsMigration2(name, taskRunnable);
        }
    }

    /**
     * Component responsible for providing the source server's configurations.
     * @param <S> the source server type
     * @param <C> the source configuration type
     */
    public interface SourceConfigurations<S extends Server, C> {
        Collection<C> getConfigurations(S source, WildFlyServer10 target);
    }
}
