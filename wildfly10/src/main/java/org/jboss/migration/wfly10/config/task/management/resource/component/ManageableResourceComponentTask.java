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

package org.jboss.migration.wfly10.config.task.management.resource.component;

import org.jboss.migration.core.task.component.ComponentTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.ComponentTaskRunnable;
import org.jboss.migration.core.task.component.NameFactory;
import org.jboss.migration.wfly10.config.management.ManageableResource;

/**
 * @author emmartins
 */
public abstract class ManageableResourceComponentTask<S, R extends ManageableResource, T extends ManageableResourceComponentTask<S, R, T>> extends ComponentTask<T> {

    private final S source;
    private final R resource;

    protected ManageableResourceComponentTask(Builder<S, R, T, ?> builder) {
        super(builder);
        this.source = builder.source;
        this.resource = builder.resource;
    }

    public S getSource() {
        return source;
    }

    public R getResource() {
        return resource;
    }

    public abstract static class Builder<S, R extends ManageableResource, T extends ManageableResourceComponentTask<S, R, T>, B extends Builder<S, R, T, B>> extends ComponentTask.Builder<T, B> {

        private S source;
        private R resource;

        protected Builder(ServerMigrationTaskName name, ComponentTaskRunnable<T> runnable) {
            super(name, runnable);
        }

        protected Builder(NameFactory<T> nameFactory, ComponentTaskRunnable<T> runnable) {
            super(nameFactory, runnable);
        }

        protected Builder(Builder<S, R, T, ?> other) {
            super(other);
            this.source = other.source;
            this.resource = other.resource;
        }

        public B source(S source) {
            this.source = source;
            return getThis();
        }

        public B resource(R resource) {
            this.resource = resource;
            return getThis();
        }
    }
}
