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

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
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
    protected abstract ServerMigrationTaskResult runTask(ServerMigrationTaskContext context) throws Exception;

    /**
     * A listener for task run related events.
     */
    public interface Listener {
        void started(ServerMigrationTaskContext context);
        void done(ServerMigrationTaskContext context);
    }

    public interface Skipper {
        boolean isSkipped(ServerMigrationTaskContext context);
    }

    /**
     * The task builder.
     */
    public static class Builder<T extends Builder> {

        private final ServerMigrationTaskName name;
        private Listener listener;
        private Skipper skipper;

        public Builder(ServerMigrationTaskName name) {
            this.name = name;
            skipTaskPropertyName(name.getName()+".skip");
        }

        @SuppressWarnings("unchecked")
        public T listener(Listener listener) {
            this.listener = listener;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T skipTaskPropertyName(final String skipTaskPropertyName) {
            return skipper(new Skipper() {
                @Override
                public boolean isSkipped(ServerMigrationTaskContext context) {
                    return skipTaskPropertyName != null ? context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(skipTaskPropertyName, Boolean.FALSE) : false;
                }
            });
        }

        @SuppressWarnings("unchecked")
        public T skipper(Skipper skipper) {
            this.skipper = skipper;
            return (T) this;
        }
    }
}
