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

        private TaskRunnable.Builder<? super P> runnableBuilder;
        
        //private SubtasksBuilder<? super P, ?> subtasks;

        protected BaseBuilder() {
        }

        protected BaseBuilder(BaseBuilder<P, ?> other) {
            super(other);
            this.runnableBuilder = other.runnableBuilder;
        }

        public T subtasks(SubtasksBaseBuilder<? super P, ?> subtasks) {
            runnableBuilder = subtasks;
            return getThis();
        }

        public <Q extends BuildParameters> T subtasks(BuildParameters.Mapper<P, Q> mapper, SubtasksBaseBuilder<? super Q, ?> subtasks) {
            runnableBuilder = TaskRunnable.Builder.from(mapper, subtasks);
            return getThis();
        }

        @Override
        protected TaskRunnable.Builder<? super P> getRunnableBuilder() {
            return runnableBuilder;
        }
    }

    protected static abstract class SubtasksBaseBuilder<P extends BuildParameters, T extends SubtasksBaseBuilder<P, T>> implements TaskRunnable.Builder<P> {

        private final List<TaskRunnable.Builder<? super P>> builders = new ArrayList<>();

        protected SubtasksBaseBuilder() {
        }

        protected SubtasksBaseBuilder(SubtasksBaseBuilder<? super P, ?> other) {
            this.builders.addAll(other.builders);
        }


        private T subtask(TaskRunnable.Builder<? super P> runnableBuilder) {
            this.builders.add(runnableBuilder);
            return getThis();
        }

        public T subtask(ServerMigrationTask task) {
            return subtask((params, taskName) -> context -> context.execute(task).getResult());
        }

        public T subtask(ComponentTask.Builder<? super P, ?> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return subtask((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        public <Q extends BuildParameters> T subtask(BuildParameters.Mapper<P, Q> mapper, ComponentTask.Builder<? super Q, ?> qBuilder) {
            return subtask((p, taskName) -> context -> {
                final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for (Q q : mapper.apply(p)) {
                    if (context.execute(qBuilder.build(q)).getResult().getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            });
        }

        @Override
        public TaskRunnable build(P params, ServerMigrationTaskName taskName) {
            return context -> {
                final ServerMigrationTaskResult.Builder result = new ServerMigrationTaskResult.Builder().skipped();
                for (TaskRunnable.Builder<? super P> builder : builders) {
                    if (builder.build(params, taskName).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        result.success();
                    }
                }
                return result.build();
            };
        }

        protected abstract T getThis();
    }
    // ----

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

    public static class SubtasksBuilder<P extends BuildParameters> extends SubtasksBaseBuilder<P, SubtasksBuilder<P>> {
        @Override
        protected SubtasksBuilder<P> getThis() {
            return this;
        }
    }

}
