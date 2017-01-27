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

package org.jboss.migration.wfly10.config.task.management.resource.composite;

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;

/**
 * @author emmartins
 */
public class ConcreteManageableResourceCompositeTask<S, R extends ManageableResource> extends AbstractManageableResourceCompositeTask<S, R> {

    protected ConcreteManageableResourceCompositeTask(Builder<S, R> builder) {
        super(builder);
    }

    public interface SubtaskExecutor<S, R extends ManageableResource> extends AbstractManageableResourceCompositeTask.SubtaskExecutor<S, R, ConcreteManageableResourceCompositeTask<S, R>> {
    }

    public interface SubtaskFactory<S, R extends ManageableResource> extends AbstractManageableResourceCompositeTask.SubtaskFactory<S, R, ConcreteManageableResourceCompositeTask<S, R>> {
    }


    public static class Builder<S, R extends ManageableResource> extends AbstractManageableResourceCompositeTask.Builder<S, R, ConcreteManageableResourceCompositeTask<S, R>, Builder<S, R>> {

        public Builder(ServerMigrationTaskName name, Class<R> resourceType) {
            super(name, resourceType);
        }

        public Builder(NameFactory<ConcreteManageableResourceCompositeTask<S, R>> nameFactory, Class<R> resourceType) {
            super(nameFactory, resourceType);
        }

        public Builder(ServerMigrationTaskName name, ManageableResourceSelector<R> selector) {
            super(name, selector);
        }

        public Builder(NameFactory<ConcreteManageableResourceCompositeTask<S, R>> nameFactory, ManageableResourceSelector<R> selector) {
            super(nameFactory, selector);
        }

        public Builder(Builder<S, R> other) {
            super(other);
        }

        @Override
        public Builder<S, R> clone() {
            return new Builder<>(this);
        }

        @Override
        public ConcreteManageableResourceCompositeTask<S, R> build() {
            return new ConcreteManageableResourceCompositeTask<>(this);
        }
    }
}
