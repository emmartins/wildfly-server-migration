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

package org.jboss.migration.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ServerMigrationTask} which delegates to subtask executors.
 * @author emmartins
 */
public abstract class ParentTask2 extends AbstractServerMigrationTask {

    protected final boolean succeedIfHasSuccessfulSubtasks;
    protected final Subtasks subtasks;

    protected ParentTask2(BaseBuilder<?> builder, Subtasks subtasks) {
        super(builder);
        this.succeedIfHasSuccessfulSubtasks = builder.succeedIfHasSuccessfulSubtasks;
        this.subtasks = Collections.unmodifiableList(builder.subtasks);
        this.contextFactories = Collections.unmodifiableList(contextFactories);
    }

    @Override
    protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
        for (ContextFactory<T> contextFactory : contextFactories) {
            final T subtasksContext = contextFactory.newInstance(taskContext);
            if (subtasksContext != null) {
                runSubtasks(subtasksContext);
            }
        }
        return (!succeedIfHasSuccessfulSubtasks || taskContext.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected void runSubtasks(T context) throws Exception {
        for (Subtasks<T> subtask : subtasks) {
            subtask.run(context);
        }
    }

    public interface Subtasks<T extends TaskContext> {
        void run(T context) throws Exception;
    }

    public interface ContextFactory<T extends TaskContext> {
        T newInstance(TaskContext context) throws Exception;
    }

    /**
     * The parent task builder.
     */
    protected static abstract class BaseBuilder<T extends TaskContext, B extends BaseBuilder<T,B>> extends Builder<B> {

        protected final List<Subtasks<T>> subtasks = new ArrayList<>();
        protected boolean succeedIfHasSuccessfulSubtasks = true;

        protected BaseBuilder(ServerMigrationTaskName name) {
            super(name);
        }

        /*
        protected BaseBuilder(BaseBuilder<?,?> other, List<Subtasks<T>> subtasks) {
            super(other);
            this.succeedIfHasSuccessfulSubtasks = other.succeedIfHasSuccessfulSubtasks;
            this.subtasks.addAll(subtasks);
        }
        */

        protected BaseBuilder(BaseBuilder<?,?> other) {
            super(other);
            this.succeedIfHasSuccessfulSubtasks = other.succeedIfHasSuccessfulSubtasks;
        }

        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new Subtasks<T>() {
                @Override
                public void run(T context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        public B subtask(Subtasks<T> subtask) {
            subtasks.add(subtask);
            return (B) this;
        }

        public B succeedIfHasSuccessfulSubtasks() {
            succeedIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            succeedIfHasSuccessfulSubtasks = false;
            return (B) this;
        }
    }
}
