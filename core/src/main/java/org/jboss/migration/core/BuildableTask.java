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

import java.util.Objects;

/**
 * A buildable {@link ServerMigrationTask} implementation.
 * @author emmartins
 */
public class BuildableTask implements ServerMigrationTask {

    protected final ServerMigrationTaskName name;
    protected final Runnable runnable;
    protected final BeforeRunListener beforeRunListener;
    protected final AfterRunListener afterRunListener;
    protected final Skipper skipper;

    protected BuildableTask(Builder<? extends Runnable, ?> builder) {
        this.name = builder.name;
        this.runnable = builder.runnable;
        this.beforeRunListener = builder.beforeRunListener;
        this.afterRunListener = builder.afterRunListener;
        this.skipper = builder.skipper;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) throws Exception {
        if (skipper != null && skipper.isSkipped(context)) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        if (beforeRunListener != null) {
            beforeRunListener.beforeRun(context);
        }
        final ServerMigrationTaskResult result = runnable.run(context);
        if (afterRunListener != null) {
            afterRunListener.afterRun(context);
        }
        return result;
    }

    /**
     * The listener invoked before task run.
     */
    public interface BeforeRunListener {
        void beforeRun(TaskContext context);
    }

    /**
     * The listener invoked after task run.
     */
    public interface AfterRunListener {
        void afterRun(TaskContext context);
    }

    /**
     * The component which runs the task's logic.
     */
    public interface Runnable {
        ServerMigrationTaskResult run(TaskContext context) throws Exception;
    }

    /**
     * The component which indicates if the task execution should be skipped.
     */
    public interface Skipper {
        boolean isSkipped(TaskContext context);
    }

    /**
     * The task builder.
     */
    public static class Builder<R extends Runnable, B extends Builder> {

        protected final ServerMigrationTaskName name;
        protected final R runnable;
        protected BeforeRunListener beforeRunListener;
        protected AfterRunListener afterRunListener;
        protected Skipper skipper;

        public Builder(ServerMigrationTaskName name, R runnable) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(runnable);
            this.name = name;
            this.runnable = runnable;
        }

        @SuppressWarnings("unchecked")
        public B beforeRun(BeforeRunListener beforeRunListener) {
            this.beforeRunListener = beforeRunListener;
            return (B) this;
        }


        @SuppressWarnings("unchecked")
        public B afterRun(AfterRunListener afterRunListener) {
            this.afterRunListener = afterRunListener;
            return (B) this;
        }

        public B defaultSkipper() {
            return skipTaskPropertyName(name+".skip");
        }

        public B skipTaskPropertyName(final String skipTaskPropertyName) {
            return skipper(context -> skipTaskPropertyName != null ? context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(skipTaskPropertyName, Boolean.FALSE) : false);
        }

        @SuppressWarnings("unchecked")
        public B skipper(Skipper skipper) {
            this.skipper = skipper;
            return (B) this;
        }

        public BuildableTask build() {
            return new BuildableTask(this);
        }
    }
}
