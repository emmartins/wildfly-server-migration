/*
 * Copyright 2016 Red Hat, Inc.
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
 * An {@link ServerMigrationTask} which delegates to subtasks.
 * @author emmartins
 */
public abstract class ParentServerMigrationTask extends AbstractServerMigrationTask {

    private final boolean succeedOnlyIfHasSuccessfulSubtasks;

    protected ParentServerMigrationTask(BaseBuilder<?> builder) {
        super(builder);
        this.succeedOnlyIfHasSuccessfulSubtasks = builder.succeedOnlyIfHasSuccessfulSubtasks;
    }

    @Override
    protected ServerMigrationTaskResult runTask(TaskContext context) throws Exception {
        runSubtasks(context);
        return (!succeedOnlyIfHasSuccessfulSubtasks || context.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected abstract void runSubtasks(TaskContext context) throws Exception;

    /**
     * The parent task builder.
     */
    public static abstract class BaseBuilder<B extends BaseBuilder> extends AbstractServerMigrationTask.Builder<B> {

        protected boolean succeedOnlyIfHasSuccessfulSubtasks = true;

        protected BaseBuilder(ServerMigrationTaskName name) {
            super(name);
        }

        public abstract B subtask(ServerMigrationTask subtask);

        public abstract B subtask(SubtaskExecutor subtask);

        public B succeedOnlyIfHasSuccessfulSubtasks() {
            succeedOnlyIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            succeedOnlyIfHasSuccessfulSubtasks = false;
            return (B) this;
        }
    }

    public static class Builder extends BaseBuilder<Builder> {

        protected final List<SubtaskExecutor> subtasks;

        public Builder(ServerMigrationTaskName name) {
            super(name);
            this.subtasks = new ArrayList<>();
        }

        @Override
        public Builder subtask(final ServerMigrationTask subtask) {
            return subtask(new SubtaskExecutor() {
                @Override
                public void executeSubtasks(TaskContext context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        @Override
        public Builder subtask(SubtaskExecutor subtask) {
            subtasks.add(subtask);
            return this;
        }

        public ParentServerMigrationTask build() {
            final List<SubtaskExecutor> subtasks = Collections.unmodifiableList(this.subtasks);
            return new ParentServerMigrationTask(this) {
                @Override
                protected void runSubtasks(TaskContext context) throws Exception {
                    for (SubtaskExecutor subtaskExecutor : subtasks) {
                        subtaskExecutor.executeSubtasks(context);
                    }
                }
            };
        }
    }

    public interface SubtaskExecutor {
        void executeSubtasks(TaskContext context) throws Exception;
    }
}
