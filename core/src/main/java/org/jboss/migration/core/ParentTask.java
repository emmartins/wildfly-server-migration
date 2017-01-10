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
public class ParentTask extends AbstractServerMigrationTask {

    protected final boolean succeedIfHasSuccessfulSubtasks;
    protected final List<Subtasks> subtasks;

    protected ParentTask(BaseBuilder<Subtasks, ?> builder) {
        this(builder, Collections.unmodifiableList(builder.subtasks));
    }

    protected ParentTask(BaseBuilder<?, ?> builder, List<Subtasks> subtasks) {
        super(builder);
        this.succeedIfHasSuccessfulSubtasks = builder.succeedIfHasSuccessfulSubtasks;
        this.subtasks = subtasks;
    }

    @Override
    protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
        runSubtasks(taskContext);
        return (!succeedIfHasSuccessfulSubtasks || taskContext.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected void runSubtasks(TaskContext context) throws Exception {
        for (Subtasks subtask : subtasks) {
            subtask.run(context);
        }
    }

    public interface Subtasks {
        void run(TaskContext context) throws Exception;
    }

    /**
     * The parent task builder.
     */
    protected static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends AbstractServerMigrationTask.Builder<B> {

        protected final List<S> subtasks = new ArrayList<>();
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

        protected BaseBuilder(BaseBuilder<?,?> other) {
            super(other);
            this.succeedIfHasSuccessfulSubtasks = other.succeedIfHasSuccessfulSubtasks;
        }
        */

        public B subtask(S subtask) {
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

    public static class Builder extends BaseBuilder<Subtasks, Builder> {

        public Builder(ServerMigrationTaskName name) {
            super(name);
        }

        public Builder subtask(final ServerMigrationTask subtask) {
            return subtask(new Subtasks() {
                @Override
                public void run(TaskContext context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        public ParentTask build() {
            return new ParentTask(this);
        }

    }
}
