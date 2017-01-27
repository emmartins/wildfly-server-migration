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

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResource;

/**
 * @author emmartins
 */
public class BasicManageableResourceComponentTask<S, R extends ManageableResource> extends ManageableResourceComponentTask<S, R, BasicManageableResourceComponentTask<S, R>> {

    private BasicManageableResourceComponentTask(Builder<S, R> builder) {
        super(builder);
    }

    @Override
    protected BasicManageableResourceComponentTask<S, R> getThis() {
        return this;
    }

    public static class Builder<S, R extends ManageableResource> extends ManageableResourceComponentTask.Builder<S, R, BasicManageableResourceComponentTask<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName name, Runnable<S, R> runnable) {
            super(name, runnable);
        }

        public Builder(NameFactory<S, R> nameFactory, Runnable<S, R> runnable) {
            super(nameFactory, runnable);
        }

        protected Builder(Builder other) {
            super(other);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder clone() {
            return new Builder(this);
        }

        @Override
        public BasicManageableResourceComponentTask<S, R> build() {
            return new BasicManageableResourceComponentTask<>(this);
        }
    }

    public interface NameFactory<S, R extends ManageableResource> extends org.jboss.migration.core.task.component.NameFactory<BasicManageableResourceComponentTask<S, R>> {
    }

    public interface Runnable<S, R extends ManageableResource> extends org.jboss.migration.core.task.component.ComponentTaskRunnable<BasicManageableResourceComponentTask<S, R>> {
    }
}
