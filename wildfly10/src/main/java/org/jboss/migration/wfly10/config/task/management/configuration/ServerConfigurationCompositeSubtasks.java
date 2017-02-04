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

import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.component.CompositeSubtasks;

/**
 * @author emmartins
 */
public class ServerConfigurationCompositeSubtasks<S> extends CompositeSubtasks<ServerConfigurationBuildParameters<S>> {

    public ServerConfigurationCompositeSubtasks(Builder<S> builder, ServerConfigurationBuildParameters<S> params, ServerMigrationTaskName taskName) {
        super(builder, params, taskName);
    }

    public static class Builder<S> extends BaseBuilder<ServerConfigurationBuildParameters<S>, Builder<S>> implements ServerConfigurationCompositeSubtasksBuilder<S, Builder<S>> {

        @Override
        protected Builder<S> getThis() {
            return this;
        }

        @Override
        public ServerConfigurationCompositeSubtasks build(ServerConfigurationBuildParameters<S> params, ServerMigrationTaskName taskName) {
            return new ServerConfigurationCompositeSubtasks(this, params, taskName);
        }
    }

    public static <S> Builder<S> of(ServerConfigurationComponentTaskBuilder<S, ?>... subtasks) {
        final Builder<S> builder = new Builder<>();
        for (ServerConfigurationComponentTaskBuilder<S, ?> subtask : subtasks) {
            builder.subtask(subtask);
        };
        return builder;
    }
}
