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
 * A {@link ServerMigrationTask} which simply execute its subtasks.
 * @author emmartins
 */
public class ParentServerMigrationTask<C extends ServerMigrationTaskContext> extends AbstractServerMigrationTask<C> {

    private final List<SubtaskExecutor> subtasks;
    private final boolean succeedOnlyIfHasSuccessfulSubtasks;

    public ParentServerMigrationTask(Builder builder) {
        super(builder);
        this.subtasks = builder.subtasks != null ? Collections.unmodifiableList(builder.subtasks) : Collections.<SubtaskExecutor>emptyList();
        this.succeedOnlyIfHasSuccessfulSubtasks = builder.succeedOnlyIfHasSuccessfulSubtasks;
    }

    @Override
    protected C getTaskContext(ServerMigrationTaskContext context) throws Exception {
        return context;
    }

    @Override
    protected ServerMigrationTaskResult runTask(C context) throws Exception {
        for (SubtaskExecutor subtaskExecutor : subtasks) {
            runSubtaskExecutor(subtaskExecutor, context);
        }
        return (!succeedOnlyIfHasSuccessfulSubtasks || context.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected void runSubtaskExecutor(SubtaskExecutor subtaskExecutor, ServerMigrationTaskContext context) throws Exception {
        subtaskExecutor.executeSubtasks(context);
    }

    /**
     * The parent task builder.
     */
    public static abstract class AbstractBuilder<C extends ServerMigrationTaskContext, B extends AbstractBuilder> extends AbstractServerMigrationTask.Builder<B> {

        protected final List<SubtaskExecutor> subtasks;
        protected boolean succeedOnlyIfHasSuccessfulSubtasks = true;

        protected AbstractBuilder(ServerMigrationTaskName name) {
            super(name);
            this.subtasks = new ArrayList<>();
        }

        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new SubtaskExecutor() {
                @Override
                public void executeSubtasks(ServerMigrationTaskContext context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        public B subtask(SubtaskExecutor subtask) {
            subtasks.add(subtask);
            return (B) this;
        }

        public B succeedOnlyIfHasSuccessfulSubtasks() {
            succeedOnlyIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            succeedOnlyIfHasSuccessfulSubtasks = false;
            return (B) this;
        }
    }

    public static class Builder<C extends ServerMigrationTaskContext> extends AbstractBuilder<C, Builder> {
        public Builder(ServerMigrationTaskName name) {
            super(name);
        }
        public ParentServerMigrationTask build() {
            return new ParentServerMigrationTask(this);
        }
    }

    public interface SubtaskExecutor<C extends ServerMigrationTaskContext> {
        void executeSubtasks(C context) throws Exception;
    }
}
