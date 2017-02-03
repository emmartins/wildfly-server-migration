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

package org.jboss.migration.wfly10.config.task.management.configuration;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.CompositeTask;
import org.jboss.migration.core.task.component.TaskRunnable;

/**
 * @author emmartins
 */
public class ServerConfigurationCompositeTask extends CompositeTask {

    protected ServerConfigurationCompositeTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        super(name, taskRunnable);
    }

    public static class Builder<S> extends BaseBuilder<ServerConfigurationBuildParameters<S>, Builder<S>> implements ServerConfigurationCompositeTaskBuilder<S, Builder<S>> {
        @Override
        protected Builder<S> getThis() {
            return this;
        }

        @Override
        protected ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
            return new ServerConfigurationCompositeTask(name, taskRunnable);
        }
    }
}
