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

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.HostsManagement;
import org.jboss.migration.wfly10.config.management.InterfacesManagement;
import org.jboss.migration.wfly10.config.task.executor.InterfacesManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.executor.ResourceManagementSubtaskExecutor;

import java.util.List;

/**
 * @author emmartins
 */
public class InterfacesManagementTask<S> extends ResourceManagementTask<S, InterfacesManagement> {

    protected InterfacesManagementTask(Builder<S> builder, S source, InterfacesManagement... resourceManagements) {
        super(builder, source, resourceManagements);
    }

    public interface Subtasks<S> extends ResourceManagementSubtaskExecutor<S, InterfacesManagement> {
    }

    public static class Builder<S> extends ResourceManagementTask.BaseBuilder<S, InterfacesManagement, Subtasks<S>, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, InterfacesManagement... resourceManagements) {
            return new InterfacesManagementTask<>(this, source, resourceManagements);
        }
    }
}