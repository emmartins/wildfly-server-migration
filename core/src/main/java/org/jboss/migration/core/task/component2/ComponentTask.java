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

    protected static abstract class Builder<P extends BuildParameters, T extends Builder<P, T>> {

        private TaskNameBuilder<? super P> taskNameBuilder;
        private TaskSkipPolicy.Builder<? super P> skipPolicyBuilder = TaskSkipPolicy.Builders.skipIfDefaultSkipPropertyIsSet();
        private BeforeTaskRun.Builder<? super P> beforeRunBuilder;
        private AfterTaskRun.Builder<? super P> afterRunBuilder;

        protected Builder() {
        }

        protected Builder(Builder<P, ?> other) {
            Objects.requireNonNull(other);
            this.taskNameBuilder = other.taskNameBuilder;
            this.skipPolicyBuilder = other.skipPolicyBuilder;
            this.beforeRunBuilder = other.beforeRunBuilder;
            this.afterRunBuilder = other.afterRunBuilder;
        }

        public T name(String name) {
            return name(new ServerMigrationTaskName.Builder(name).build());
        }

        public T name(ServerMigrationTaskName name) {
            return name(parameters -> name);
        }

        public T name(TaskNameBuilder<? super P> builder) {
            this.taskNameBuilder = builder;
            return getThis();
        }

        public T skipPolicy(TaskSkipPolicy skipPolicy) {
            return skipPolicy((parameters, name) -> skipPolicy);
        }

        public T skipPolicy(TaskSkipPolicy.Builder<? super P> builder) {
            this.skipPolicyBuilder = builder;
            return getThis();
        }

        public T beforeRun(BeforeTaskRun beforeRun) {
            return beforeRun((parameters, name) -> beforeRun);
        }

        public T beforeRun(BeforeTaskRun.Builder<? super P> builder) {
            this.beforeRunBuilder = builder;
            return getThis();
        }

        public T afterRun(AfterTaskRun afterRun) {
            return afterRun((parameters, name) -> afterRun);
        }

        public T afterRun(AfterTaskRun.Builder<? super P> builder) {
            this.afterRunBuilder = builder;
            return getThis();
        }

        protected ServerMigrationTaskName buildName(P parameters) {
            Objects.requireNonNull(taskNameBuilder);
            return taskNameBuilder.build(parameters);
        }

        protected TaskRunnable buildRunnable(P parameters, ServerMigrationTaskName taskName) {

            final TaskRunnable.Builder<? super P> runnableBuilder = getRunnableBuilder();
            Objects.requireNonNull(runnableBuilder);
            final TaskRunnable runnable = runnableBuilder.build(parameters, taskName);
            final TaskSkipPolicy skipPolicy = skipPolicyBuilder != null ? skipPolicyBuilder.build(parameters, taskName) : null;
            final BeforeTaskRun beforeRun = beforeRunBuilder != null ? beforeRunBuilder.build(parameters, taskName) : null;
            final AfterTaskRun afterRun = afterRunBuilder != null ? afterRunBuilder.build(parameters, taskName) : null;
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

        public <P1 extends P> ServerMigrationTask build(P1 params) {
            final ServerMigrationTaskName taskName = buildName(params);
            return buildTask(taskName, buildRunnable(params, taskName));
        }

        protected abstract T clone();
        protected abstract T getThis();
        protected abstract TaskRunnable.Builder<? super P> getRunnableBuilder();
        protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable);


    }
}
