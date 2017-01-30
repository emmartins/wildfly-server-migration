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

    public static <P extends BuildParameters> Builder<P, ?> builder() {
        return new BuilderImpl<>();
    }

    public interface Builder<P extends BuildParameters, T extends Builder<P, T>> extends ComponentTask.Builder<P,T> {

        default T run(ServerMigrationTask task) {
            return run((params, taskName) -> context -> context.execute(task).getResult());
        }

        default T run(ComponentTask.Builder<? super P, ?> builder) {
            final ComponentTask.Builder<? super P, ?> clone = builder.clone();
            return run((params, taskName) -> context -> context.execute(clone.build(params)).getResult());
        }

        default <Q extends BuildParameters> T run(BuildParameters.Mapper<P, Q> parametersMapper, ComponentTask.Builder<? super Q, ?> q) {
            return run(TaskRunnable.Adapters.of(parametersMapper, q));
        }
    }

    protected static abstract class AbstractBuilder<P extends BuildParameters, T extends AbstractBuilder<P, T>> extends ComponentTask.AbstractBuilder<P, T> implements Builder<P,T> {

        private final List<TaskRunnable.Builder<? super P>> runnableBuilders = new ArrayList<>();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(AbstractBuilder<P, ?> other) {
            super(other);
            this.runnableBuilders.addAll(other.runnableBuilders);
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

    protected static class BuilderImpl<P extends BuildParameters> extends AbstractBuilder<P, BuilderImpl<P>> {

        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(BuilderImpl<P> other) {
            super(other);
        }

        @Override
        public BuilderImpl<P> clone() {
            return new BuilderImpl(this);
        }

        @Override
        protected BuilderImpl<P> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new CompositeTask(name, taskRunnable);
        }
    }
}
