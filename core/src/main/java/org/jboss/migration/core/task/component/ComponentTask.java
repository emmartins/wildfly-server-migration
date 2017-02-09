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
public abstract class ComponentTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final TaskRunnable taskRunnable;

    protected ComponentTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(taskRunnable);
        this.name = name;
        this.taskRunnable = taskRunnable;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        return taskRunnable.run(context);
    }

    public abstract static class Builder<P extends BuildParameters, T extends Builder<P, T>> implements ComponentTaskBuilder<P, T> {

        private TaskNameBuilder<? super P> taskNameBuilder;
        private TaskSkipPolicy.Builder<? super P> skipPolicyBuilder;
        private BeforeTaskRun.Builder<? super P> beforeRunBuilder;
        private AfterTaskRun.Builder<? super P> afterRunBuilder;

        protected Builder() {
        }

        /*
        protected Builder(Builder<P, ?> other) {
            Objects.requireNonNull(other);
            this.taskNameBuilder = other.taskNameBuilder;
            this.skipPolicyBuilder = other.skipPolicyBuilder;
            this.beforeRunBuilder = other.beforeRunBuilder;
            this.afterRunBuilder = other.afterRunBuilder;
        }
        */

        @Override
        public T nameBuilder(TaskNameBuilder<? super P> builder) {
            this.taskNameBuilder = builder;
            return getThis();
        }

        protected TaskNameBuilder<? super P> getTaskNameBuilder() {
            return taskNameBuilder;
        }

        @Override
        public T skipPolicyBuilder(TaskSkipPolicy.Builder<? super P> builder) {
            this.skipPolicyBuilder = builder;
            return getThis();
        }

        protected TaskSkipPolicy.Builder<? super P> getSkipPolicyBuilder() {
            return skipPolicyBuilder;
        }

        @Override
        public T beforeRunBuilder(BeforeTaskRun.Builder<? super P> builder) {
            this.beforeRunBuilder = builder;
            return getThis();
        }

        protected BeforeTaskRun.Builder<? super P> getBeforeRunBuilder() {
            return beforeRunBuilder;
        }

        @Override
        public T afterRunBuilder(AfterTaskRun.Builder<? super P> builder) {
            this.afterRunBuilder = builder;
            return getThis();
        }

        protected AfterTaskRun.Builder<? super P> getAfterRunBuilder() {
            return afterRunBuilder;
        }

        protected ServerMigrationTaskName buildName(P parameters) {
            Objects.requireNonNull(taskNameBuilder);
            return taskNameBuilder.build(parameters);
        }

        protected TaskRunnable buildRunnable(P parameters) {
            final TaskRunnable.Builder<? super P> runnableBuilder = getRunnableBuilder();
            Objects.requireNonNull(runnableBuilder);
            final TaskRunnable runnable = runnableBuilder.build(parameters);
            final TaskSkipPolicy.Builder<? super P> skipPolicyBuilder = getSkipPolicyBuilder();
            final TaskSkipPolicy skipPolicy = skipPolicyBuilder != null ? skipPolicyBuilder.build(parameters) : null;
            final BeforeTaskRun.Builder<? super P> beforeRunBuilder = getBeforeRunBuilder();
            final BeforeTaskRun beforeRun = beforeRunBuilder != null ? beforeRunBuilder.build(parameters) : null;
            final AfterTaskRun.Builder<? super P> afterRunBuilder = getAfterRunBuilder();
            final AfterTaskRun afterRun = afterRunBuilder != null ? afterRunBuilder.build(parameters) : null;
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

        public ServerMigrationTask build(P params) {
            return buildTask(buildName(params), buildRunnable(params));
        }

        protected abstract T getThis();

        protected abstract TaskRunnable.Builder<? super P> getRunnableBuilder();

        protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable);
    }
}
