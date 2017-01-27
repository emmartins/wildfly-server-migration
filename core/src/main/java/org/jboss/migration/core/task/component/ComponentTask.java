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
public abstract class ComponentTask<T extends ComponentTask<T>> implements ServerMigrationTask {

    private final NameFactory<T> nameFactory;
    private final ComponentTaskRunnable<T> runnable;
    private final SkipPolicy<T> skipPolicy;
    private final BeforeRunListener<T> beforeRunListener;
    private final AfterRunListener<T> afterRunListener;

    protected ComponentTask(Builder<T, ?> builder) {
        nameFactory = builder.nameFactory;
        Objects.requireNonNull(nameFactory);
        runnable = builder.runnable;
        Objects.requireNonNull(runnable);
        skipPolicy = builder.skipPolicy;
        beforeRunListener = builder.beforeRunListener;
        afterRunListener = builder.afterRunListener;
    }

    protected abstract T getThis();

    @Override
    public ServerMigrationTaskName getName() {
        return nameFactory.getName(getThis());
    }

    protected ComponentTaskRunnable<T> getRunnable() {
        return runnable;
    }

    protected AfterRunListener<T> getAfterRunListener() {
        return afterRunListener;
    }

    protected BeforeRunListener<T> getBeforeRunListener() {
        return beforeRunListener;
    }

    protected SkipPolicy<T> getSkipPolicy() {
        return skipPolicy;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) throws Exception {
        final SkipPolicy<T> skipPolicy = getSkipPolicy();
        if (skipPolicy != null && skipPolicy.isSkipped(getThis(), context)) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        final BeforeRunListener<T> beforeRunListener = getBeforeRunListener();
        if (beforeRunListener != null) {
            beforeRunListener.beforeRun(getThis(), context);
        }
        final ServerMigrationTaskResult result = getRunnable().run(getThis(), context);
        final AfterRunListener<T> afterRunListener = getAfterRunListener();
        if (afterRunListener != null) {
            afterRunListener.afterRun(getThis(), context);
        }
        return result;
    }

    protected static abstract class Builder<T extends ComponentTask, B extends Builder<T, B>> {

        protected final NameFactory<T> nameFactory;
        protected final ComponentTaskRunnable<T> runnable;
        protected SkipPolicy<T> skipPolicy;
        protected BeforeRunListener<T> beforeRunListener;
        protected AfterRunListener<T> afterRunListener;

        protected Builder(ServerMigrationTaskName name, ComponentTaskRunnable<T> runnable) {
            this(task -> name, runnable);
        }

        protected Builder(NameFactory<T> nameFactory, ComponentTaskRunnable<T> runnable) {
            this.nameFactory = nameFactory;
            this.runnable = runnable;
        }

        protected Builder(Builder<T, ?> other) {
            this.nameFactory = other.nameFactory;
            this.runnable = other.runnable;
            this.skipPolicy = other.skipPolicy;
            this.beforeRunListener = other.beforeRunListener;
            this.afterRunListener = other.afterRunListener;
        }

        public B afterRun(AfterRunListener<T> listener) {
            this.afterRunListener = listener;
            return getThis();
        }

        public B beforeRun(BeforeRunListener<T> listener) {
            this.beforeRunListener = listener;
            return getThis();
        }

        public B skipPolicy(SkipPolicy<T> skipper) {
            this.skipPolicy = skipper;
            return getThis();
        }

        public B defaultSkipPolicy() {
            return skipPolicy((task, context) -> context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(task.getName().getName() + ".skip", Boolean.FALSE));
        }

        public B skipIfAnyPropertySet(String... propertyNames) {
            return skipPolicy((task, context) -> {
                for (String propertyName : propertyNames) {
                    if (context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(propertyName, Boolean.FALSE)) {
                        return true;
                    }
                }
                return false;
            });
        }

        protected abstract B getThis();
        public abstract B clone();
        public abstract T build();
    }
}