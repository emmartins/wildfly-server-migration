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

/**
 * @author emmartins
 */
public class LeafTask extends ComponentTask {

    protected LeafTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    public static <P extends BuildParameters> Builder<P, ?> builder() {
        return new BuilderImpl<>();
    }

    public interface Builder<P extends BuildParameters, T extends Builder<P, T>> extends ComponentTask.Builder<P,T> {
    }

    protected static abstract class AbstractBuilder<P extends BuildParameters, T extends AbstractBuilder<P, T>> extends ComponentTask.AbstractBuilder<P, T> implements Builder<P, T> {

        private TaskRunnable.Builder<? super P> runnableBuilder;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(AbstractBuilder<P, ?> other) {
            super(other);
            this.runnableBuilder = other.runnableBuilder;
        }

        @Override
        public T run(TaskRunnable.Builder<? super P> builder) {
            this.runnableBuilder = builder;
            return getThis();
        }

        @Override
        public TaskRunnable.Builder<? super P> getRunnableBuilder() {
            return runnableBuilder;
        }
    }

    protected static class BuilderImpl<P extends BuildParameters> extends AbstractBuilder<P, BuilderImpl<P>> {

        protected BuilderImpl() {
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
            return new LeafTask(name, taskRunnable);
        }
    }
}
