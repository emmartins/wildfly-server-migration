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

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class CompositeTaskRunnable implements TaskRunnable {

    private final TaskRunnable[] runnables;

    protected <T extends BaseBuilder<T>> CompositeTaskRunnable(BaseBuilder<T> builder) {
        this.runnables = builder.runnables.stream().toArray(TaskRunnable[]::new);
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
        for (TaskRunnable runnable : runnables) {
            switch (runnable.run(context).getStatus()) {
                case FAIL:
                    throw new ServerMigrationFailureException("the execution of a child runnable returned fail result");
                case SUCCESS:
                    resultBuilder.success();
                    break;
                default:
                    break;
            }
        }
        return resultBuilder.build();
    }

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {

        private final List<TaskRunnable> runnables;

        protected BaseBuilder() {
            runnables = new ArrayList<>();
        }

        protected abstract T getThis();

        public T runnable(TaskRunnable runnable) {
            runnables.add(runnable);
            return getThis();
        }

        public T runnables(TaskRunnable... runnables) {
            for (TaskRunnable runnable : runnables) {
                runnable(runnable);
            }
            return getThis();
        }

        public CompositeTaskRunnable build() {
            return new CompositeTaskRunnable(this);
        }
    }

    public static class Builder extends BaseBuilder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
