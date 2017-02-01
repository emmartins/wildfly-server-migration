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

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class CompositeTask extends ComponentTask {

    protected CompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    protected static abstract class BaseBuilder<P extends BuildParameters, T extends BaseBuilder<P, T>> extends ComponentTask.Builder<P, T> {

        private final List<TaskRunnable.Builder<? super P>> runnableBuilders = new ArrayList<>();

        protected BaseBuilder() {
        }

        protected BaseBuilder(BaseBuilder<P, ?> other) {
            super(other);
            this.runnableBuilders.addAll(other.runnableBuilders);
        }

        public T subtask(ServerMigrationTask task) {
            return run((params, taskName) -> context -> context.execute(task).getResult());
        }

        public T subtask(ComponentTask.Builder<? super P, ?> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return run((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        public  <Q extends BuildParameters> T subtask(BuildParameters.Mapper<P, Q> mapper, ComponentTask.Builder<? super Q, ?> builder) {
            return run(TaskRunnable.Builder.from(mapper, builder));
        }

        @Override
        public T run(TaskRunnable.Builder<? super P> runnableBuilder) {
            this.runnableBuilders.add(runnableBuilder);
            return getThis();
        }

        @Override
        public TaskRunnable.Builder<? super P> getRunnableBuilder() {
            final List<TaskRunnable.Builder<? super P>> runnableBuildersCopy = new ArrayList<>(this.runnableBuilders);
            final TaskRunnable.Builder<P> compositeRunnableBuilder = (params, name) -> context -> {
                for (TaskRunnable.Builder<? super P> runnableBuilder : runnableBuildersCopy) {
                    runnableBuilder.build(params, name).run(context);
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            };
            return compositeRunnableBuilder;
        }
    }

    public static class Builder<P extends BuildParameters> extends BaseBuilder<P, Builder<P>> {

        public Builder() {
        }

        protected Builder(Builder<P> other) {
            super(other);
        }

        @Override
        public Builder<P> clone() {
            return new Builder(this);
        }

        @Override
        protected Builder<P> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new CompositeTask(name, taskRunnable);
        }
    }
}
