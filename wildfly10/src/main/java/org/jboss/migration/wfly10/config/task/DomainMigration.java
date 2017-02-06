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
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.CompositeTask;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.core.task.component.TaskSkipPolicy;

/**
 * The domain migration (composite) task.
 * @author emmartins
 */
public class DomainMigration extends CompositeTask {

    protected DomainMigration(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    /**
     * Base abstract builder implementation.
     * @param <S> the source server type
     * @param <P>
     * @param <T>
     */
    protected static abstract class BaseBuilder<S extends Server, P extends ServerMigrationParameters<S>, T extends BaseBuilder<S, P, T>> extends CompositeTask.BaseBuilder<P, T> {

        private DomainConfigurationsMigration<S, ?> domainConfigurationsMigration;
        private HostConfigurationsMigration<S, ?> hostConfigurationsMigration;

        protected BaseBuilder() {
            name("domain");
            skipPolicyBuilder(buildParameters -> TaskSkipPolicy.skipIfAnySkips(
                    TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(),
                    TaskSkipPolicy.skipIfNoUserConfirmation("Setup the target's domain?")));
            beforeRun(context -> {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n");
                context.getLogger().infof("Domain migration starting...");
            });
            afterRun(context -> {
                context.getServerMigrationContext().getConsoleWrapper().printf("%n");
                context.getLogger().infof("Domain migration done.");
            });
        }

        public T domainConfigurations(DomainConfigurationsMigration<S, ?> domainConfigurationsMigration) {
            this.domainConfigurationsMigration = domainConfigurationsMigration;
            return getThis();
        }

        public T hostConfigurations(HostConfigurationsMigration<S, ?> hostConfigurationsMigration) {
            this.hostConfigurationsMigration = hostConfigurationsMigration;
            return getThis();
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new DomainMigration(name, taskRunnable);
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
    }
}
