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

package org.jboss.migration.core.task.composite;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.ComponentTask;
import org.jboss.migration.core.task.component.NameFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ComponentTask} which runnable delegates to subtasks.
 * @author emmartins
 */
public abstract class CompositeTask<T extends CompositeTask<T>> extends ComponentTask<T> {

    protected CompositeTask(Builder<T, ?> builder) {
        super(builder);
    }

    /**
     * The parent task extensible builder.
     */
    protected static abstract class Builder<T extends CompositeTask, B extends Builder<T, B>> extends ComponentTask.Builder<T, B> {

        protected final CompositeTaskRunnable<T> runnable;

        protected Builder(ServerMigrationTaskName name, CompositeTaskRunnable<T> runnable) {
            super(name, runnable);
            this.runnable = runnable;
        }

        protected Builder(NameFactory<T> nameFactory, CompositeTaskRunnable<T> runnable) {
            super(nameFactory, runnable);
            this.runnable = runnable;
        }

        protected Builder(Builder<T, ?> other) {
            super(other);
            this.runnable = other.runnable;
        }

        public B succeedIfHasSuccessfulSubtasks() {
            runnable.succeedIfHasSuccessfulSubtasks();
            return getThis();
        }

        public B succeedAlways() {
            runnable.succeedAlways();
            return getThis();
        }

        public B subtask(ServerMigrationTask subtask) {
            runnable.subtask(subtask);
            return getThis();
        }

        public B subtask(SubtaskExecutor<T> subtask) {
            runnable.subtask(subtask);
            return getThis();
        }

        public B subtask(SubtaskFactory<T> subtask) {
            runnable.subtask(subtask);
            return getThis();
        }
    }

    protected static class RunnableImpl<T extends CompositeTask> implements CompositeTaskRunnable<T> {

        private boolean succeedIfHasSuccessfulSubtasks = true;
        private final List<SubtaskExecutor<T>> subtaskExecutors;

        protected RunnableImpl() {
            this.subtaskExecutors = new ArrayList<>();
        }

        public void succeedIfHasSuccessfulSubtasks() {
            succeedIfHasSuccessfulSubtasks = true;
        }

        public void succeedAlways() {
            succeedIfHasSuccessfulSubtasks = false;
        }

        public void subtask(ServerMigrationTask subtask) {
            final SubtaskExecutor<T> subtaskExecutor = (parentTask, parentTaskContext) -> parentTaskContext.execute(subtask);
            subtask(subtaskExecutor);
        }

        public void subtask(SubtaskExecutor<T> subtaskExecutor) {
            this.subtaskExecutors.add(subtaskExecutor);
        }

        public void subtask(SubtaskFactory<T> subtaskFactory) {
            final SubtaskExecutor<T> subtaskExecutor = (parentTask, parentTaskContext) -> parentTaskContext.execute(subtaskFactory.getTask(parentTask, parentTaskContext));
            subtask(subtaskExecutor);
        }

        @Override
        public ServerMigrationTaskResult run(T task, TaskContext context) throws Exception {
            for (SubtaskExecutor<T> subtaskExecutor : subtaskExecutors) {
                subtaskExecutor.run(task, context);
            }
            return (!succeedIfHasSuccessfulSubtasks || context.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        }
    }
}
