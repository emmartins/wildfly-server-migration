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

package org.jboss.migration.wfly10.config.task.factory;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.JvmResources;
import org.jboss.migration.wfly10.config.task.executor.ManageableResourceSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;

/**
 * @author emmartins
 */
public class JVMsManagementTask<S> extends ManageableResourceTask<S, JvmResources> {

    protected JVMsManagementTask(Builder<S> builder, S source, JvmResources... resourceManagements) {
        super(builder, source, resourceManagements);
    }

    public interface Subtasks<S> extends ManageableResourceSubtaskExecutor<S, JvmResources> {
    }

    public static class Builder<S> extends ManageableResourceTask.BaseBuilder<S, JvmResources, Subtasks<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, JvmResources... resourceManagements) {
            return new JVMsManagementTask<>(this, source, resourceManagements);
        }
    }
}