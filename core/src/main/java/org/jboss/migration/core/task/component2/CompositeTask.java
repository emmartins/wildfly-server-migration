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
import java.util.Objects;

/**
 * @author emmartins
 */
public class CompositeTask extends ComponentTask {

    protected CompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    protected static abstract class BaseBuilder<P extends BuildParameters, T extends BaseBuilder<P, T>> extends ComponentTask.Builder<P, T> {

        private Subtasks<P> subtasks;

        protected BaseBuilder() {
        }

        protected BaseBuilder(BaseBuilder<P, ?> other) {
            super(other);
            this.subtasks = new Subtasks<>(other.subtasks);
        }

        protected T subtasks(Subtasks<P> subtasks) {
            this.subtasks = subtasks;
            return getThis();
        }

        @Override
        public TaskRunnable.Builder<? super P> getRunnableBuilder() {
            Objects.requireNonNull(subtasks);
            return subtasks;
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

    public static class Subtasks<P extends BuildParameters> implements TaskRunnable.Builder<P> {

        private final List<TaskRunnable.Builder<? super P>> builders = new ArrayList<>();

        public Subtasks() {
        }

        public Subtasks(Subtasks<P> other) {
            builders.addAll(other.builders);
        }

        // ---

        protected Subtasks<P> run(TaskRunnable.Builder<? super P> runnableBuilder) {
            this.builders.add(runnableBuilder);
            return this;
        }

        public Subtasks<P> run(Subtasks<P> other) {
            for (TaskRunnable.Builder<? super P> subtask : other.builders) {
                run(subtask);
            }
            return this;
        }

        public Subtasks<P> run(ServerMigrationTask task) {
            return run((params, taskName) -> context -> context.execute(task).getResult());
        }

        public Subtasks<P> run(ComponentTask.Builder<? super P, ?> builder) {
            final ComponentTask.Builder clone = builder.clone();
            return run((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        public <Q extends BuildParameters> Subtasks<P> run(BuildParameters.Mapper<P, Q> mapper, ComponentTask.Builder<? super Q, ?> qBuilder) {
            return run((p, taskName) -> context -> {
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
    }

    /*
    protected static class RunnableBuilder<P extends BuildParameters> implements TaskRunnable.Builder<P> {

        private final List<TaskRunnable.Builder<? super P>> runnableBuilders = new ArrayList<>();

        public RunnableBuilder() {
        }

        protected RunnableBuilder(RunnableBuilder<P> other) {
            this.runnableBuilders.addAll(other.runnableBuilders);
        }

        protected void add(TaskRunnable.Builder<? super P> runnableBuilder) {
            this.runnableBuilders.add(runnableBuilder);
        }

        protected <Q extends BuildParameters> void add(BuildParameters.Mapper<P, Q> mapper, TaskRunnable.Builder<? super Q> qBuilder) {
            add((p, taskName) -> context -> {
                final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().skipped();
                for (Q q : mapper.apply(p)) {
                    if (qBuilder.build(q, taskName).run(context).getStatus() == ServerMigrationTaskResult.Status.SUCCESS) {
                        resultBuilder.success();
                    }
                }
                return resultBuilder.build();
            });
        }

        @Override
        public TaskRunnable build(P params, ServerMigrationTaskName taskName) {
            final List<TaskRunnable.Builder<? super P>> builders = new ArrayList<>(this.runnableBuilders);
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
    }
    */
}
