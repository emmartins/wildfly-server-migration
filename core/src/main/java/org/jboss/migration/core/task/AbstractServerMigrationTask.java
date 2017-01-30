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

package org.jboss.migration.core.task;

import org.jboss.migration.core.ServerMigrationFailureException;

/**
 * An abstract {@link ServerMigrationTask} implementation.
 * @author emmartins
 */
public abstract class AbstractServerMigrationTask implements ServerMigrationTask {

    protected final ServerMigrationTaskName name;
    protected final Listener listener;
    protected final Skipper skipper;

    protected AbstractServerMigrationTask(Builder builder) {
        this.name = builder.name;
        this.listener = builder.listener;
        this.skipper = builder.skipper;
    }

    protected AbstractServerMigrationTask(ServerMigrationTaskName name) {
        this(new Builder(name));
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) throws ServerMigrationFailureException {
        if (skipper != null && skipper.isSkipped(context)) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        if (listener != null) {
            listener.started(context);
        }
        final ServerMigrationTaskResult result = runTask(context);
        if (listener != null) {
            listener.done(context);
        }
        return result;
    }

    /**
     * The concrete task running logic.
     * @param context
     * @return
     * @throws Exception
     */
    protected abstract ServerMigrationTaskResult runTask(TaskContext context) throws ServerMigrationFailureException;

    /**
     * A listener for task run related events.
     */
    public interface Listener {
        void started(TaskContext context);
        void done(TaskContext context);
    }

    public interface Skipper {
        boolean isSkipped(TaskContext context);
    }

    /**
     * The task builder.
     */
    public static class Builder<B extends Builder> {

        protected final ServerMigrationTaskName name;
        protected Listener listener;
        protected Skipper skipper;

        public Builder(ServerMigrationTaskName name) {
            this.name = name;
            skipTaskPropertyName(name.getName()+".skip");
        }

        public Builder(Builder other) {
            this.name = other.name;
            this.listener = other.listener;
            this.skipper = other.skipper;
        }

        @SuppressWarnings("unchecked")
        public B listener(Listener listener) {
            this.listener = listener;
            return (B) this;
        }

        public B skipTaskPropertyName(String skipTaskPropertyName) {
            return skipper(context -> skipTaskPropertyName != null ? context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(skipTaskPropertyName, Boolean.FALSE) : false);
        }

        @SuppressWarnings("unchecked")
        public B skipper(Skipper skipper) {
            this.skipper = skipper;
            return (B) this;
        }
    }
}
