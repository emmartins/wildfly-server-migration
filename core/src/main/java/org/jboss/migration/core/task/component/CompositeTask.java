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

/**
 * @author emmartins
 */
public class CompositeTask extends ComponentTask {

    protected CompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    protected static abstract class BaseBuilder<P extends BuildParameters, T extends BaseBuilder<P, T>> extends ComponentTask.Builder<P, T> implements CompositeTaskBuilder<P, T> {

        private TaskRunnable.Builder<? super P> runnableBuilder;

        protected T runBuilder(TaskRunnable.Builder<? super P> subtasks) {
            this.runnableBuilder = subtasks;
            return getThis();
        }

        @Override
        public T subtasks(CompositeSubtasksBuilder<? super P, ?> subtasks) {
            return runBuilder(subtasks);
        }

        @Override
        public <Q extends BuildParameters> T subtasks(BuildParameters.Mapper<P, Q> mapper, CompositeSubtasksBuilder<? super Q, ?> subtasks) {
            return runBuilder(TaskRunnable.Builder.from(mapper, subtasks));
        }

        @Override
        protected TaskRunnable.Builder<? super P> getRunnableBuilder() {
            return runnableBuilder;
        }
    }

    public static class Builder<P extends BuildParameters> extends BaseBuilder<P, Builder<P>> {

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
