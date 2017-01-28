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

package org.jboss.migration.core.task.component2;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.Objects;

/**
 * @author emmartins
 */
public abstract class ComponentTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final Runnable runnable;

    protected ComponentTask(ServerMigrationTaskName name, Runnable runnable) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(runnable);
        this.name = name;
        this.runnable = runnable;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) throws Exception {
        return runnable.run(name, context);
    }

    protected abstract static class Builder<P extends Parameters, T extends Builder<P, T>> implements ComponentTaskBuilder<P, T> {

        private NameFactory<P> nameFactory;
        private SkipPolicy<P> skipPolicy;
        private BeforeRun<P> beforeRun;
        private AfterRun<P> afterRun;

        protected Builder() {
        }

        protected Builder(Builder<P, ?> other) {
            Objects.requireNonNull(other);
            this.nameFactory = other.nameFactory;
            this.skipPolicy = other.skipPolicy;
            this.beforeRun = other.beforeRun;
            this.afterRun = other.afterRun;
        }

        @Override
        public T name(NameFactory<P> nameFactory) {
            this.nameFactory = nameFactory;
            return getThis();
        }

        @Override
        public T skipPolicy(SkipPolicy skipPolicy) {
            this.skipPolicy = skipPolicy;
            return getThis();
        }

        @Override
        public T beforeRun(BeforeRun beforeRun) {
            this.beforeRun = beforeRun;
            return getThis();
        }

        @Override
        public T afterRun(AfterRun afterRun) {
            this.afterRun = afterRun;
            return getThis();
        }

        protected ServerMigrationTaskName buildName(P parameters) {
            Objects.requireNonNull(nameFactory);
            return nameFactory.newInstance(parameters);
        }

        protected Runnable buildRunnable(P parameters) {
            final RunnableFactory<P> runnableFactory = getRunnableFactory();
            Objects.requireNonNull(runnableFactory);
            final SkipPolicy<P> skipPolicy = this.skipPolicy;
            final BeforeRun<P> beforeRun = this.beforeRun;
            final AfterRun<P> afterRun = this.afterRun;
            return (taskName, context) -> {
                if (skipPolicy != null && skipPolicy.isSkipped(parameters, taskName, context)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (beforeRun != null) {
                    beforeRun.beforeRun(parameters, taskName, context);
                }
                final ServerMigrationTaskResult result = runnableFactory.newInstance(parameters).run(taskName, context);
                if (afterRun != null) {
                    afterRun.afterRun(parameters, taskName, context);
                }
                return result;
            };
        }

        @Override
        public ServerMigrationTask build(P parameters) throws Exception {
            return buildTask(buildName(parameters), buildRunnable(parameters));
        }

        protected abstract T getThis();
        protected abstract RunnableFactory<P> getRunnableFactory();
        protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, Runnable runnable);
    }
}
