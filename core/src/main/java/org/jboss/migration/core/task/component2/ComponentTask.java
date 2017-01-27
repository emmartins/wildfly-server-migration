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
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.Objects;

/**
 * @author emmartins
 */
public abstract class ComponentTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final Runnable runnable;

    protected ComponentTask(ServerMigrationTaskName name, Runnable runnable) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(runnable);
        this.name = name;
        this.runnable = runnable;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) throws Exception {
        return runnable.run(name, context);
    }

    public abstract static class Builder<P extends Parameters, T extends Builder<P, T>> implements ComponentTaskBuilder<P, T> {

        private NameFactory<P> nameFactory;
        private SkipPolicy<P> skipPolicy;


        @Override
        public T name(NameFactory<P> nameFactory) {
            return null;
        }

        @Override
        public T skipPolicy(SkipPolicy skipPolicy) {
            return null;
        }

        @Override
        public T beforeRun(BeforeRun beforeRun) {
            return null;
        }

        @Override
        public T afterRun(AfterRun afterRun) {
            return null;
        }


        protected abstract T getThis();
    }
}
