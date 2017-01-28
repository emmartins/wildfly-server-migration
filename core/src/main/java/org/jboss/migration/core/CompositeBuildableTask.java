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
import java.util.Collection;
import java.util.List;

/**
 * A {@link ServerMigrationTask} which delegates to subtask executors.
 * @author emmartins
 */
public class CompositeBuildableTask extends BuildableTask {

    protected CompositeBuildableTask(Builder builder) {
        super(builder);
    }

    protected static class Runnable implements BuildableTask.Runnable {

        private boolean succeedIfHasSuccessfulSubtasks = true;
        protected final List<ServerMigrationTask> subtasks = new ArrayList<>();

        @Override
        public ServerMigrationTaskResult run(TaskContext context) throws Exception {
            runSubtasks(context);
            return (!succeedIfHasSuccessfulSubtasks || context.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        }

        protected void runSubtasks(TaskContext context) throws Exception {
            for (ServerMigrationTask subtask : subtasks) {
                context.execute(subtask);
            }
        }
    }

    /**
     * The parent task extensible builder.
     */
    protected static class Builder<B extends Builder<B>> extends BuildableTask.Builder<Runnable, B> {

        public Builder(ServerMigrationTaskName name) {
            this(name, new Runnable());
        }

        public Builder(ServerMigrationTaskName name, Runnable runnable) {
            super(name, runnable);
        }

        public B succeedIfHasSuccessfulSubtasks() {
            runnable.succeedIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            runnable.succeedIfHasSuccessfulSubtasks = false;
            return (B) this;
        }

        public B subtask(ServerMigrationTask subtask) {
            runnable.subtasks.add(subtask);
            return (B) this;
        }

        @Override
        public CompositeBuildableTask build() {
            return new CompositeBuildableTask(this);
        }
    }
}
