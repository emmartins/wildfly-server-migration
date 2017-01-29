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

import java.util.Objects;

/**
 * @author emmartins
 */
public abstract class AbstractTaskBuilder <P extends TaskBuilder.Params, T extends AbstractTaskBuilder<P, T>> implements TaskBuilder<P, T> {

    private NameFactory<? super P> nameFactory;
    private SkipPolicy<? super P> skipPolicy;
    private BeforeRun<? super P> beforeRun;
    private AfterRun<? super P> afterRun;

    protected AbstractTaskBuilder() {
    }

    protected AbstractTaskBuilder(AbstractTaskBuilder<P, ?> other) {
        Objects.requireNonNull(other);
        this.nameFactory = other.nameFactory;
        this.skipPolicy = other.skipPolicy;
        this.beforeRun = other.beforeRun;
        this.afterRun = other.afterRun;
    }

    @Override
    public T name(NameFactory<? super P> nameFactory) {
        this.nameFactory = nameFactory;
        return getThis();
    }

    @Override
    public T skipPolicy(SkipPolicy<? super P> skipPolicy) {
        this.skipPolicy = skipPolicy;
        return getThis();
    }

    @Override
    public T beforeRun(BeforeRun<? super P> beforeRun) {
        this.beforeRun = beforeRun;
        return getThis();
    }

    @Override
    public T afterRun(AfterRun<? super P> afterRun) {
        this.afterRun = afterRun;
        return getThis();
    }

    protected ServerMigrationTaskName buildName(P parameters) throws Exception {
        Objects.requireNonNull(nameFactory);
        return nameFactory.newInstance(parameters);
    }

    protected TaskRunnable buildRunnable(P parameters) {
        final RunnableFactory<? super P> runnableFactory = getRunnableFactory();
        Objects.requireNonNull(runnableFactory);
        final SkipPolicy<? super P> skipPolicy = this.skipPolicy;
        final BeforeRun<? super P> beforeRun = this.beforeRun;
        final AfterRun<? super P> afterRun = this.afterRun;
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
    public <Q extends P> ServerMigrationTask build(Q params) throws Exception {
        return buildTask(buildName(params), buildRunnable(params));
    }

    protected abstract T getThis();
    protected abstract RunnableFactory<? super P> getRunnableFactory();
    protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable);
}
