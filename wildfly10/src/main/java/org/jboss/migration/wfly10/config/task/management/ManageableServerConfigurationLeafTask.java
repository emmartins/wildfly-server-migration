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

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class ManageableServerConfigurationLeafTask<S, T extends ManageableServerConfiguration> extends ManageableResourceLeafTask<S, T> {

    public ManageableServerConfigurationLeafTask(BaseBuilder<S, T, ?> builder, S source, T resource) {
        super(builder, source, resource);
    }

    public interface Runnable<S, T extends ManageableServerConfiguration> extends ManageableResourceLeafTask.Runnable<S, T> {
    }

    public static abstract class BaseBuilder<S, T extends ManageableServerConfiguration, B extends BaseBuilder<S, T, B>> extends ManageableResourceLeafTask.BaseBuilder<S, T, B> {
        public BaseBuilder(ServerMigrationTaskName name, Runnable<S, T> runnable) {
            super(name, runnable);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, ManageableServerConfiguration, Builder<S>> {
        public Builder(ServerMigrationTaskName name, Runnable<S, ManageableServerConfiguration> runnable) {
            super(name, runnable);
        }
        @Override
        public ServerMigrationTask build(S source, ManageableServerConfiguration resource) {
            return new ManageableServerConfigurationLeafTask(this, source, resource);
        }
    }
}
