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

package org.jboss.migration.core.task.component;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.Objects;

/**
 * @author emmartins
 */
public class SimpleComponentTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final TaskRunnable taskRunnable;

    protected SimpleComponentTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        this.name = Objects.requireNonNull(name);
        this.taskRunnable = Objects.requireNonNull(taskRunnable);
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        return taskRunnable.run(context);
    }

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> implements SimpleComponentTaskBuilder<T> {

        private ServerMigrationTaskName name;
        private TaskSkipPolicy skipPolicy;
        private BeforeTaskRun beforeRun;
        private TaskRunnable runnable;
        private AfterTaskRun afterRun;

        protected BaseBuilder() {
        }

        protected ServerMigrationTaskName getName() {
            return name;
        }

        @Override
        public T name(ServerMigrationTaskName name) {
            this.name = name;
            return getThis();
        }

        protected TaskSkipPolicy getSkipPolicy() {
            return skipPolicy;
        }

        @Override
        public T skipPolicy(TaskSkipPolicy skipPolicy) {
            this.skipPolicy = skipPolicy;
            return getThis();
        }

        protected BeforeTaskRun getBeforeRun() {
            return beforeRun;
        }

        @Override
        public T beforeRun(BeforeTaskRun beforeRun) {
            this.beforeRun = beforeRun;
            return getThis();
        }

        protected TaskRunnable getRunnable() {
            return runnable;
        }

        @Override
        public T runnable(TaskRunnable runnable) {
            this.runnable = runnable;
            return getThis();
        }

        protected AfterTaskRun getAfterRun() {
            return afterRun;
        }

        @Override
        public T afterRun(AfterTaskRun afterRun) {
            this.afterRun = afterRun;
            return getThis();
        }

        protected TaskRunnable buildRunnable() {
            Objects.requireNonNull(runnable);
            return context -> {
                if (skipPolicy != null && skipPolicy.isSkipped(context)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (beforeRun != null) {
                    beforeRun.beforeRun(context);
                }
                final ServerMigrationTaskResult result = runnable.run(context);
                if (afterRun != null) {
                    afterRun.afterRun(context);
                }
                return result;
            };
        }

        public ServerMigrationTask build() {
            return buildTask(Objects.requireNonNull(name), buildRunnable());
        }

        protected abstract T getThis();

        protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable runnable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable runnable) {
            return new SimpleComponentTask(name, runnable);
        }
    }
}
