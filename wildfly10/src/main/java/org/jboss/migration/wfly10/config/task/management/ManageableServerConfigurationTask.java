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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import java.util.Collection;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationTask<S, T extends ManageableServerConfiguration> extends ManageableResourceTask<S, T> {

    protected ManageableServerConfigurationTask(BaseBuilder<S, T, ?> builder, S source, Collection<? extends T> resources) {
        super(builder, source, resources);
    }

    public interface SubtaskExecutor<S, T extends ManageableServerConfiguration> extends ManageableResourceTask.SubtaskExecutor<S, T> {
    }

    public static abstract class BaseBuilder<S, T extends ManageableServerConfiguration, B extends BaseBuilder<S, T, B>> extends ManageableResourceTask.BaseBuilder<S, T, B> {
        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, ManageableServerConfiguration, Builder<S>> {
        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableServerConfiguration> resources) {
            return new ManageableServerConfigurationTask<>(this, source, resources);
        }
    }
}
